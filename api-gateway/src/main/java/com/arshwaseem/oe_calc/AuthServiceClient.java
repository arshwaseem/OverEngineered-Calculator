package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.configuration.AuthServiceProperties;
import com.arshwaseem.oe_calc.dto.TokenValidationRequest;
import com.arshwaseem.oe_calc.dto.TokenValidationResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final AuthServiceProperties authServiceProperties;

    public TokenValidationResponse validateToken(String token, HttpServletRequest originalRequest) {
        log.debug("Validating token");

        try{
            TokenValidationRequest request = new TokenValidationRequest(token);

            String cookieHeader = Arrays.stream(originalRequest.getCookies()).map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining(";"));

            if(cookieHeader.isEmpty()){
                throw new RuntimeException("Invalid token");
            }

            return webClientBuilder.build()
                    .post()
                    .uri(authServiceProperties.getUrl()+"/auth/validate")
                    .header(HttpHeaders.COOKIE, cookieHeader)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Error Validating Token: "+e.getMessage());

            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Failed to validate: "+e.getMessage())
                    .build();
        }
    }

}