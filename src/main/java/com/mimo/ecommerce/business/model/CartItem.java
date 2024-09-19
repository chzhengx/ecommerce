package com.mimo.ecommerce.business.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Created by zcl on 2024/9/9.
 */
@Data
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    private Integer quantity;
}