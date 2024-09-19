package com.mimo.ecommerce.business.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Created by zcl on 2024/9/9.
 */
@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String imageUrl;

    /**
     * 乐观锁的版本号，用于防止并发更新
     */
    @Version
    private Integer version;
}
