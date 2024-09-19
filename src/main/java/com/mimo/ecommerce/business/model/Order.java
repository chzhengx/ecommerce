package com.mimo.ecommerce.business.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by zcl on 2024/9/9.
 */
@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单的所属用户
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 订单中的商品项
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<CartItem> items;

    /**
     * 订单总价
     */
    private Double totalPrice;
    /**
     * 订单状态（如 "pending", "shipped", "delivered"）
     */
    private String status;
    private Date orderDate;
}