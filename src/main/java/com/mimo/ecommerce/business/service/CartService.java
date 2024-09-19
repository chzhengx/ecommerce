package com.mimo.ecommerce.business.service;

import com.mimo.ecommerce.business.model.Cart;
import com.mimo.ecommerce.business.model.CartItem;
import com.mimo.ecommerce.business.model.Product;
import com.mimo.ecommerce.business.model.User;
import com.mimo.ecommerce.business.repository.CartRepository;
import com.mimo.ecommerce.business.repository.ProductRepository;
import org.springframework.stereotype.Service;

/**
 * Created by zcl on 2024/9/9.
 */
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart validateCart(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
            if (product == null || item.getQuantity() > product.getStock()) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock. Available stock: " + product.getStock());
            }
        }
        return cart;
    }
}