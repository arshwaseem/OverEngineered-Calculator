package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.configuration.AuthServiceProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthProxyController {

    private final WebClient.Builder webClientBuilder;
    private final AuthServiceProperties authServiceProperties;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Object registerRequest) {
        log.debug("Proxying Request To Auth Service");

        try{
            Object response = webClientBuilder.build()
                    .post()
                    .uri(authServiceProperties.getUrl()+"/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(registerRequest)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e){
            log.error("Failed to Register User: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falied to register user: "+ e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Object loginRequest, HttpServletResponse httpResponse){
        log.debug("Proxying Login Request to auth service");

        try{
            ResponseEntity<Object> response = webClientBuilder.build()
                    .post()
                    .uri(authServiceProperties.getUrl()+"/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(loginRequest)
                    .retrieve()
                    .toEntity(Object.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if(response == null){
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
            log.error("Failed to login: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse){
        log.debug("Proxying Refresh Request to auth service");
        try{
            String cookieHeader = extractCookieHeader(httpRequest);

            ResponseEntity<Object> response = webClientBuilder.build()
                    .post()
                    .uri(authServiceProperties.getUrl()+"/auth/refresh")
                    .header(HttpHeaders.SET_COOKIE, cookieHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(Object.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if(response==null){
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
            log.error("Failed to refresh token: "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to refresh token: "+e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse){
        log.debug("Proxying Logout Request to auth service");

        try{
            ResponseEntity<Object> response = webClientBuilder.build()
                    .post()
                    .uri(authServiceProperties.getUrl()+"/auth/logout")
                    .retrieve()
                    .toEntity(Object.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if(response==null){
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
            log.error("Failed to logout token: "+e.getMessage());
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
