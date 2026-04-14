package com.duoc.backend;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.duoc.backend.Constants.AUTHORITIES_CLAIM;
import static com.duoc.backend.Constants.HEADER_AUTHORIZACION_KEY;
import static com.duoc.backend.Constants.TOKEN_BEARER_PREFIX;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (isJwtValid(request)) {
                Claims claims = getClaims(request);
                setAuthentication(claims);
            } else {
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, exception.getMessage());
        }
    }

    private boolean isJwtValid(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(HEADER_AUTHORIZACION_KEY);
        return authenticationHeader != null && authenticationHeader.startsWith(TOKEN_BEARER_PREFIX);
    }

    private Claims getClaims(HttpServletRequest request) {
        String jwtToken = request.getHeader(HEADER_AUTHORIZACION_KEY).replace(TOKEN_BEARER_PREFIX, "");
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    private void setAuthentication(Claims claims) {
        List<String> authorities = (List<String>) claims.getOrDefault(AUTHORITIES_CLAIM, Collections.emptyList());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
