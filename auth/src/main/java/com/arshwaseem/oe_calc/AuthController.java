package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.*;
import com.arshwaseem.oe_calc.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());
        try {
            RegisterResponse res = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            log.error("Error registering new user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error registering new user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Logining user: {}", loginRequest.getUsername());
        try {
            AuthResponse res = authService.login(loginRequest);

            cookieUtil.setAuthCookie(response, res.getAccessToken(), res.getRefreshToken());

            AuthResponse sanitizedResponse = AuthResponse.builder()
                    .tokenType(res.getTokenType())
                    .expiresIn(res.getExpiresIn())
                    .username(res.getUsername())
                    .userId(res.getUserId())
                    .accessToken(res.getAccessToken())
                    .refreshToken(res.getRefreshToken())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(sanitizedResponse);
        } catch (Exception e) {
            log.error("Error logining user: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error logining user: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {

        log.info("Token Validation Request...");

        String token = cookieUtil.getAccessTokenCookie(request).orElse(null);

        try{

            if (token == null) {
                log.info("Token not found in cookie, expecting in request body");
                return ResponseEntity.badRequest()
                        .body(TokenValidationResponse.builder()
                                .valid(false)
                                .message("No Token Found in cookie or request body")
                                .build());
            }

            log.info("Token found in cookie,validating..... : {}", token);
            TokenValidationResponse res = authService.validateToken(token);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error validating token: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        log.debug("Refresh token request");

        try{
            String refreshToken = cookieUtil.getRefreshTokenCookie(request).orElseThrow(()-> new IllegalArgumentException("Refresh token not found in cookies"));

            RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
            AuthResponse res = authService.refreshToken(refreshRequest);

            cookieUtil.setAccessTokenCookie(response, res.getAccessToken());

            AuthResponse sanitizedResponse = AuthResponse.builder()
                    .tokenType(res.getTokenType())
                    .expiresIn(res.getExpiresIn())
                    .username(res.getUsername())
                    .userId(res.getUserId())
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(sanitizedResponse);
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error refreshing token: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response){
        log.debug("Logout request...");

        cookieUtil.clearAuthCookie(response);

        return ResponseEntity.status(HttpStatus.OK).body(new LogoutResponse("Successfully logged out"));
    }

    record LogoutResponse(String message) {}
}