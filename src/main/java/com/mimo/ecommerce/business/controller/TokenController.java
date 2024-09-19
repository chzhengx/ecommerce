package com.mimo.ecommerce.business.controller;

import com.mimo.ecommerce.core.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zcl on 2024/9/10.
 */
@RestController
@RequestMapping("/api/token")
public class TokenController {

    private final JWTUtils jwtUtils;

    @Autowired
    public TokenController(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtUtils.extractUsername(refreshToken);

        // 验证刷新令牌
        if (jwtUtils.isTokenValid(refreshToken, username)) {
            String newAccessToken = jwtUtils.generateAccessToken(username);
            return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken));
        }
        return ResponseEntity.status(403).body("Invalid or expired refresh token");
    }
}

class TokenRefreshRequest {
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

class TokenRefreshResponse {
    private String accessToken;

    public TokenRefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}