package com.mimo.ecommerce.business.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Created by zcl on 2024/9/9.
 */
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; // 默认角色为普通用户
}