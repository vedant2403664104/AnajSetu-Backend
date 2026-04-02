package com.example.anajsetu.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // ── Generate JWT token ─────────────────────────────────────
    public String generateToken(String phone, String role, Integer userId) {
        return Jwts.builder()
                .subject(phone)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    // ── Extract phone from token ───────────────────────────────
    public String extractPhone(String token) {
        return extractClaims(token).getSubject();
    }

    // ── Extract role from token ────────────────────────────────
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // ── Extract userId from token ──────────────────────────────
    public Integer extractUserId(String token) {
        return extractClaims(token).get("userId", Integer.class);
    }

    // ── Check if token is valid ────────────────────────────────
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Internal helpers ───────────────────────────────────────
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}