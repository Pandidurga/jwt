package com.sunware.api_gateway.filter;

import com.sunware.api_gateway.util.JwtUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Bypass JWT processing for public endpoints like /auth/**
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header is missing or does not contain Bearer token.");
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Authorization header missing or invalid.");
            return;
        }

        String jwtToken = authorizationHeader.substring(7);
        String email;
        Set<String> permissions;

        try {
            logger.info("Processing JWT token.");
            email = jwtUtil.extractEmail(jwtToken);
            permissions = jwtUtil.extractPermissions(jwtToken);

            if (email == null || permissions == null) {
                logger.warn("Email or permissions extracted from token are null.");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token.");
                return;
            }

            List<GrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            logger.debug("Token details: email={}, permissions={}", email, permissions);

            if (jwtUtil.validateToken(jwtToken)) {
                logger.info("Token is valid. Setting authentication context for user: {}", email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Successfully authenticated user: {}", email);

            } else {
                logger.warn("Token validation failed for user: {}", email);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token.");
                return;
            }

        } catch (Exception e) {
            logger.error("Error while processing JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token.");
            return;
        }

        logger.debug("Proceeding with the request for user: {}", email);
        chain.doFilter(request, response);
    }


    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(statusCode);
        String jsonResponse = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", getErrorTitle(statusCode), message);
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }

    private String getErrorTitle(int statusCode) {
        switch (statusCode) {
            case HttpServletResponse.SC_BAD_REQUEST:
                return "Bad Request";
            case HttpServletResponse.SC_UNAUTHORIZED:
                return "Unauthorized";
            default:
                return "Error";
        }
    }
}
