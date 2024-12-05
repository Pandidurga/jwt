package com.sunware.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.sunware.api_gateway.model.Permission;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.algorithm}")
    private String algorithm;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME_MS;

    private SignatureAlgorithm ALGORITHM;

    @PostConstruct
    public void init() {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalArgumentException("JWT Secret key is not set in application properties");
        }

        ALGORITHM = SignatureAlgorithm.forName(algorithm);

        if (EXPIRATION_TIME_MS <= 0) {
            throw new IllegalArgumentException("JWT Expiration time is not set or invalid.");
        }
    }

    // Generate token with permissions
    public String generateTokenWithPermissions(String email, Set<Permission> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("permissions", permissions); // Embedding permissions in the token
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(ALGORITHM, SECRET_KEY)
                .compact();
    }

    // Validate token by checking expiration and email existence
    public Boolean validateToken(String token) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail != null && !isTokenExpired(token));
    }

    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract permissions from the token
    public Set<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);

        Object permissionsObj = claims.get("permissions");
        if (permissionsObj instanceof List) {
            List<?> permissionsList = (List<?>) permissionsObj;
            return permissionsList.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf) // Convert each item to a String
                    .collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException("Permissions claim is missing or invalid");
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or malformed JWT token");
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
