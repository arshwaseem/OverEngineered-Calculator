package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.UserDTO;
import com.arshwaseem.oe_calc.configuration.UserServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final UserServiceProperties userServiceProperties;

    public UserDTO getUserByUsername(String username) {
        log.info("Fetching User with Username: " + username);

        try{
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceProperties.getUrl() + "/user/username?username=" + username)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e){
            log.error("Error Fetching From User Service: {}", e.getMessage());
            return null;
        }
    }

    public ResponseEntity<?> createUser(UserDTO userDTO) {
        log.info("Creating User with Username: " + userDTO.getUsername());

        try{
            return webClientBuilder.build()
                    .post()
                    .uri(userServiceProperties.getUrl()+"/user/register")
                    .bodyValue(userDTO)
                    .retrieve()
                    .bodyToMono(ResponseEntity.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e){
            log.error("Error Creating User with Username: {}, Error: {}",userDTO.getUsername(), e.getMessage());
            return null;
        }
    }
}
