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

    private final WebClient userServiceWebClient;

    public UserDTO getUserByUsername(String username) {
        log.debug("Fetching User with Username: " + username);

        try{
            return userServiceWebClient
                    .get()
                    .uri("/user/username?username=" + username)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .contextCapture()
                    .block();
        } catch (Exception e){
            log.error("Error Fetching From User Service: {}", e.getMessage());
            return null;
        }
    }

    public ResponseEntity<?> createUser(UserDTO userDTO) {
        log.debug("Creating User with Username: " + userDTO.getUsername());

        try{
            return userServiceWebClient
                    .post()
                    .uri("/user/register")
                    .bodyValue(userDTO)
                    .retrieve()
                    .toEntity(Void.class)
                    .contextCapture()
                    .block();
        } catch (Exception e){
            log.error("Error Creating User with Username: {}, Error: {}",userDTO.getUsername(), e.getMessage());
            return null;
        }
    }
}
