package com.mimo.ecommerce.business.service;

import com.mimo.ecommerce.business.model.User;
import com.mimo.ecommerce.business.repository.UserRepository;
import com.mimo.ecommerce.core.utils.JWTUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by zcl on 2024/9/9.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 密码加密
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && checkPassword(user, rawPassword)) {
            return jwtUtils.generateAccessToken(user.getEmail());
        }
        return null;
    }
}