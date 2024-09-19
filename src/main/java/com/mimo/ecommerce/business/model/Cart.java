package com.mimo.ecommerce.business.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Created by zcl on 2024/9/9.
 */
@Data
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<CartItem> items;

    @OneToOne
    private User user;
}