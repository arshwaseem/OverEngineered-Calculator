package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.*;
import com.arshwaseem.oe_calc.exception.AuthenticationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void auth_withValidCredentials_ShouldLogin(){

        LoginRequest loginRequest = new LoginRequest("test","pass");
        UserDTO userDTO = new UserDTO(1L,"test","pass");

        when(userServiceClient.getUserByUsername("test")).thenReturn(userDTO);
        when(passwordEncoder.matches("pass",userDTO.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("jwt-token", response.getAccessToken());
        Assertions.assertEquals("Bearer", response.getTokenType());
        Assertions.assertEquals(userDTO.getUsername(), response.getUsername());
        Assertions.assertEquals(1L, response.getUserId());

    }

    @Test
    void authWithInvalidPassword_ShouldNotLogin(){
        LoginRequest loginRequest = new LoginRequest("test","wrong_pass");
        UserDTO userDTO = new UserDTO(1L,"test","pass");

        when(userServiceClient.getUserByUsername("test")).thenReturn(userDTO);
        when(passwordEncoder.matches(loginRequest.getPassword(), userDTO.getPassword())).thenReturn(false);

        Assertions.assertThrows(AuthenticationException.class,  ()->authService.login(loginRequest));
    }

    @Test
    void authShouldThrowException_whenUserNotExists(){
        LoginRequest loginRequest = new LoginRequest("test","pass");

        when(userServiceClient.getUserByUsername("test")).thenReturn(null);

        Assertions.assertThrows(AuthenticationException.class,  ()->authService.login(loginRequest));
    }

    @Test
    void auth_ShouldRegisterNewUserAndReturnToken(){

        RegisterRequest registerRequest = new RegisterRequest("test","pass");

        UserDTO userDTO = new UserDTO(null,registerRequest.getUsername(),registerRequest.getPassword());

        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("pass");
        when(userServiceClient.createUser(userDTO)).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        RegisterResponse response = authService.register(registerRequest);

        Assertions.assertEquals(userDTO.getUsername(), response.getUsername());
        Assertions.assertEquals("User registered successfully", response.getMessage());

    }

}
