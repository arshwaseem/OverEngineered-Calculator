package com.arshwaseem.oe_calc.filter;

import com.arshwaseem.oe_calc.AuthServiceClient;
import com.arshwaseem.oe_calc.dto.TokenValidationResponse;
import com.arshwaseem.oe_calc.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/auth/register",
            "/auth/login",
            "/auth/logout",
            "/auth/refresh",
            "/auth/validate",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/refresh",
            "/api/auth/validate"
    );
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if(isPublicEndpoint(requestURI)) {
            log.debug("Public endpoint accessed, request will go through");
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if(token==null){
            log.warn("Missing token or authorization header for request : {}", request.getRequestURI());
            sendUnauthorizedError(response, "Missing authentication token");
            return;
        }

        try {
            TokenValidationResponse res = authServiceClient.validateToken(token, request);

            if(!res.isValid()){
                log.error("Invalid token");
                sendUnauthorizedError(response, res.getMessage());
                return;
            }

            request.setAttribute("userId", res.getUserId());
            request.setAttribute("username", res.getUsername());

            log.debug("Request authenticated");

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error authenticating: " +e.getMessage());
            sendUnauthorizedError(response, "Token validation failed");
        }
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\"}",
                message
        ));
    }

    private boolean isPublicEndpoint(String requestUri) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(requestUri::startsWith);
    }

    private String extractToken(HttpServletRequest request) {

        Optional<String> cookieToken = cookieUtil.getAccessToken(request);

        if(cookieToken.isPresent()) {
            log.debug("Cookie token found");
            return cookieToken.get();
        }

        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
