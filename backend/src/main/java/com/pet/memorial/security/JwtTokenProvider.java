package com.pet.memorial.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:changeMeToAtLeast32CharsSecretKey}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        String normalizedSecret = secret;
        if (normalizedSecret.length() < 32) {
            normalizedSecret = normalizedSecret + "_pet_memorial_default_secret_padding";
        }
        this.secretKey = Keys.hmacShaKeyFor(normalizedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expiration);
        String authorities = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", authorities)
            .issuedAt(issuedAt)
            .expiration(expiresAt)
                .signWith(secretKey)
            .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
