package com.mimo.ecommerce.business.service;

import com.mimo.ecommerce.business.model.Cart;
import com.mimo.ecommerce.business.model.CartItem;
import com.mimo.ecommerce.business.model.Order;
import com.mimo.ecommerce.business.model.User;
import com.mimo.ecommerce.business.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by zcl on 2024/9/9.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final IdempotencyService idempotencyService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, IdempotencyService idempotencyService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.idempotencyService = idempotencyService;
        this.productService = productService;
    }

    @Transactional
    public Order createOrder(User user, Cart cart, String requestId) {
        // 检查幂等性
        if (idempotencyService.isRequestProcessed(requestId)) {
            throw new RuntimeException("Request has already been processed");
        }

        // 遍历购物车中的商品，执行库存扣减
        cart.getItems().forEach(cartItem -> {
            productService.reduceStockWithRetry(cartItem.getProduct().getId(), cartItem.getQuantity());
        });

        // 创建订单
        Order order = new Order();
        order.setUser(user);
        order.setItems(cart.getItems());
        order.setTotalPrice(calculateTotalPrice(cart.getItems()));
        order.setStatus("pending");
        order.setOrderDate(new Date());

        // 标记请求已处理
        idempotencyService.markRequestProcessed(requestId);

        return orderRepository.save(order); // 保存订单
    }

    private Double calculateTotalPrice(List<CartItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }
}