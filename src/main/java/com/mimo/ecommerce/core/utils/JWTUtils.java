package com.mimo.ecommerce.core.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Created by zcl on 2024/9/9.
 */
@Component
public class JWTUtils {

    /**
     * 生成一个更安全的密钥，长度至少为256位，以便与HS256一起使用
     */
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 30; // 30 分钟
    private final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7 天

    /**
     * 生成 JWT 访问令牌
     */
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username) // 设置主题
                .issuedAt(new Date()) // 设置签发时间
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY)) // 30 分钟过期
                .signWith(SECRET_KEY) // 使用密钥
                .compact();
    }

    /**
     * 生成 JWT 刷新令牌
     */
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY)) // 7 天过期
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析 JWT 并提取声明
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) // 使用密钥验证签名
                .build() // 构建解析器
                .parseClaimsJws(token) // 解析令牌
                .getBody(); // 获取声明
    }

    /**
     * 从令牌中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 验证令牌是否有效
     */
    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    /**
     * 检查令牌是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}