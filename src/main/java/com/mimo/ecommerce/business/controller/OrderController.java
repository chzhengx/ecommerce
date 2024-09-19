package com.mimo.ecommerce.business.controller;

import com.mimo.ecommerce.business.model.Cart;
import com.mimo.ecommerce.business.model.Order;
import com.mimo.ecommerce.business.model.User;
import com.mimo.ecommerce.business.service.CartService;
import com.mimo.ecommerce.business.service.IdempotencyService;
import com.mimo.ecommerce.business.service.OrderService;
import com.mimo.ecommerce.business.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by zcl on 2024/9/9.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final IdempotencyService idempotencyService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService, CartService cartService, IdempotencyService idempotencyService) {
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestParam String email, @RequestParam(required = false) String requestId) {
        // 如果没有传递 requestId，则自动生成
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString(); // 生成新的 requestId
        }

        // 幂等性检查：检查是否已经处理过此请求
        if (idempotencyService.isRequestProcessed(requestId)) {
            return ResponseEntity.badRequest().body(null); // 请求已处理过，返回错误
        }

        // 获取用户和购物车
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Cart cart = cartService.getCartByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        // 验证购物车中的商品数量
        try {
            cartService.validateCart(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // 创建订单
        Order order = orderService.createOrder(user, cart, requestId);
        return ResponseEntity.ok(order);
    }

    /**
     * 获取当前用户的所有订单
     */
    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@RequestParam String email) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
        List<Order> orders = orderService.getOrdersByUser(user);
        return ResponseEntity.ok(orders);
    }

    /**
     * 获取单个订单
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 更新订单状态
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedOrder);
    }
}