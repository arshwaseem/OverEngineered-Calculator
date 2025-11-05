package com.arshwaseem.oe_calc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthProxyController {

    private final WebClient authServiceWebclient;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Object registerRequest) {
        log.debug("Proxying Request To Auth Service");

        try{
            Object response = authServiceWebclient
                    .post()
                    .uri("/auth/register")
                    .bodyValue(registerRequest)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e){
            log.error("Failed to Register User: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falied to register user: "+ e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Object loginRequest, HttpServletResponse httpResponse){
        log.debug("Proxying Login Request to auth service");

        try{
            ResponseEntity<Object> response = authServiceWebclient
                    .post()
                    .uri("/auth/login")
                    .bodyValue(loginRequest)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();

            if(response == null){
                log.error("No response from auth service");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from auth service");
            }

            HttpHeaders authHeaders = response.getHeaders();

            if(authHeaders.containsKey(HttpHeaders.SET_COOKIE)){
                authHeaders.get(HttpHeaders.SET_COOKIE).forEach(cookie -> {
                    httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie);
                    log.debug("Forwarding Cookie : {}", cookie);
                });
            }

            return ResponseEntity.status(HttpStatus.OK).body(response.getBody());

        } catch (Exception e){
            log.error("Failed to login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse){
        log.debug("Proxying Refresh Request to auth service");
        try{
            String cookieHeader = extractCookieHeader(httpRequest);

            ResponseEntity<Object> response = authServiceWebclient
                    .post()
                    .uri("/auth/refresh")
                    .header(HttpHeaders.COOKIE, cookieHeader)
                    .retrieve()
                    .toEntity(Object.class)
                    .block();

            if(response==null){
                log.error("No response from auth service");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from auth service");
            }

            HttpHeaders authHeaders = response.getHeaders();
            if(authHeaders.containsKey(HttpHeaders.SET_COOKIE)){
                authHeaders.get(HttpHeaders.SET_COOKIE).forEach(cookie -> {
                    httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie);
                    log.debug("Forwarding Cookie : {}", cookie);
                });
            }

            return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
        } catch (Exception e){
            log.error("Failed to refresh token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to refresh token: "+e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse){
        log.debug("Proxying Logout Request to auth service");

        try{
            ResponseEntity<Object> response = authServiceWebclient
                    .post()
                    .uri("/auth/logout")
                    .retrieve()
                    .toEntity(Object.class)
                    .block();

            if(response==null){
                log.error("No response from auth service");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from auth service");
            }

            HttpHeaders authHeaders = response.getHeaders();
            if(authHeaders.containsKey(HttpHeaders.SET_COOKIE)){
                authHeaders.get(HttpHeaders.SET_COOKIE).forEach(cookie -> {
                    httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie);
                    log.debug("Clearing Cookie: {}", cookie);
                });
            }

            return ResponseEntity.status(HttpStatus.OK).body(response.getBody());
        } catch (Exception e){
            log.error("Failed to logout token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to logout : " + e.getMessage());
        }
    }


    private String extractCookieHeader(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return "";
        }

        return Arrays.stream(request.getCookies())
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }
}
