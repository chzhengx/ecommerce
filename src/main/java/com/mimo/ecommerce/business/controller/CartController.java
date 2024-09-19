package com.mimo.ecommerce.business.controller;

import com.mimo.ecommerce.business.model.Cart;
import com.mimo.ecommerce.business.model.User;
import com.mimo.ecommerce.business.service.CartService;
import com.mimo.ecommerce.business.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by zcl on 2024/9/9.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Cart> getUserCart(@RequestParam String email) {
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Cart cart = cartService.getCartByUser(user);
        return ResponseEntity.ok(cart);
    }

    @PostMapping
    public ResponseEntity<Cart> updateCart(@RequestBody Cart cart) {
        cartService.validateCart(cart);
        Cart savedCart = cartService.saveCart(cart);
        return ResponseEntity.ok(savedCart);
    }
}