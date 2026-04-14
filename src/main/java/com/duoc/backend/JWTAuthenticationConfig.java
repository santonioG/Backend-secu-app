package com.duoc.backend;

import static com.duoc.backend.Constants.AUTHORITIES_CLAIM;
import static com.duoc.backend.Constants.ISSUER_INFO;
import static com.duoc.backend.Constants.TOKEN_BEARER_PREFIX;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

@Configuration
public class JWTAuthenticationConfig {

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.jwt-expiration-ms}")
    private long jwtExpirationMs;

    public String getJWTToken(String username) {
        validateSecurityConfiguration();

        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_CLAIM, grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuer(ISSUER_INFO)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .and()
                .signWith(getSigningKey())
                .compact();

        return TOKEN_BEARER_PREFIX + token;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Fails fast when the application starts issuing tokens without a real secret.
    private void validateSecurityConfiguration() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        if (jwtExpirationMs <= 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero");
        }
    }
}
