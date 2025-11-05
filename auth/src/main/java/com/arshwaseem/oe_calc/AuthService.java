package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.*;
import com.arshwaseem.oe_calc.exception.AuthenticationException;
import com.arshwaseem.oe_calc.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Register request for user: {}", registerRequest.getUsername());

        try {
            String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(registerRequest.getUsername());
            userDTO.setPassword(encodedPassword);

            ResponseEntity<?> resp = userServiceClient.createUser(userDTO);

            if (resp == null || !resp.getStatusCode().isSameCodeAs(HttpStatus.CREATED)) {
                log.error("Failed to create user");
                throw new AuthenticationException("Failed To Create User Account");
            }

            log.info("Successfully created user: {}", registerRequest.getUsername());

            return RegisterResponse.builder()
                    .message("User registered successfully")
                    .username(registerRequest.getUsername())
                    .build();
        } catch (AuthenticationException e) {
            log.error("Error while registering user: {}", e.getMessage());
            throw e;
        } catch (Exception e){
            log.error("Failed to register user: {}",e.getMessage());
            throw e;
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        log.debug("Login request for user: {}", loginRequest.getUsername());

        try {

            UserDTO user = userServiceClient.getUserByUsername(loginRequest.getUsername());

            if(user == null) {
                log.error("Unable to get user");
                throw new AuthenticationException("Failed To Login User: User not found, please check your username and password");
            }

            if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.error("Failed To Verify Password");
                throw new AuthenticationException("Failed To Login User: Incorrect password");
            }

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("Successfully logged in");

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .username(user.getUsername())
                    .userId(user.getId())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Error while logging in: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to login user: "+e.getMessage());
            throw e;
        }
    }

    public TokenValidationResponse validateToken(String token) {
        log.debug("Validate token for user: {}", jwtService.extractUsername(token));

        try{
            if(!jwtService.validateToken(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid Token")
                        .build();
            }

            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);

            log.info("Token validation successful for user {}", username);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .username(username)
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            log.error("Unable to validate Token : "+ e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Unable to verify Token: "+e.getMessage())
                    .build();
        }
    }

    public AuthResponse refreshToken (RefreshTokenRequest refreshTokenRequest) {
        log.debug("Refresh token request received");

        String refreshToken = refreshTokenRequest.getRefreshToken();

        if(!jwtService.validateToken(refreshToken)){
            log.error("Invalid Refresh Token");
            throw new InvalidTokenException("Invalid Refresh Token");
        }

        if(!jwtService.isRefreshToken(refreshToken)){
            log.error("Provided token is not a refresh token");
            throw new InvalidTokenException("Not a refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        Long userId = jwtService.extractUserId(refreshToken);

        UserDTO user = userServiceClient.getUserByUsername(username);

        if(user == null){
            log.error("User not found");
            throw new AuthenticationException("User not found");
        }

        String newAccessToken = jwtService.generateToken(user);

        log.info("Token succesfully refreshed for user : {}", username);

        return AuthResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .username(username)
                .userId(userId)
                .build();
    }

}
