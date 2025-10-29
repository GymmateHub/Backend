package com.gymmate.shared.security;

import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private Key key;

    @Autowired
    private JwtService jwtService;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user, UUID tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("tenantId", tenantId != null ? tenantId.toString() : null)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // New overload to keep callers that only pass a User working.
    public String generateToken(User user) {
        UUID tenantId = null;
        try {
            tenantId = TenantContext.getCurrentTenantId();
        } catch (Exception e) {
            // Fall back to null
        }
        return generateToken(user, tenantId);
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs * 24); // Longer expiration for refresh token

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        return jwtService.extractUserId(token);
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
