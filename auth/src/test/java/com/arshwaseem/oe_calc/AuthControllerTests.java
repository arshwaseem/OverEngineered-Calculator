package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.*;
import com.arshwaseem.oe_calc.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Test
    void auth_loginWithValidCredentials_ShouldLogin() throws Exception {

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername("test");
        authResponse.setAccessToken("jwt-token");
        authResponse.setRefreshToken("refresh-token");
        authResponse.setExpiresIn(3600);
        authResponse.setTokenType("Bearer");

        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        "username": "test",
                                        "password": "pass"
                                        }""")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("test"));
    }

    @Test
    void auth_ShouldRegisterNewUser() throws Exception {

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setUsername("testuser");
        registerResponse.setMessage("success");

        when(authService.register(any())).thenReturn(registerResponse);

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                        "username": "testuser",
                                        "password": "pass"
                                        }""")
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void auth_ShouldValidateToken_whenTokenPresent() throws Exception {

        TokenValidationResponse tokenValidationResponse = new TokenValidationResponse(true, "test", 1L, "success");

        when(authService.validateToken("valid-token")).thenReturn(tokenValidationResponse);
        when(cookieUtil.getAccessTokenCookie(any())).thenReturn(Optional.of("valid-token"));

        mockMvc.perform(
                post("/auth/validate")
                        .cookie(new Cookie("accessToken", "valid-token"))
        ).andExpect(status().isOk());

    }

    @Test
    void auth_ShouldNotValidate_whenTokenMissing() throws Exception {

        mockMvc.perform(
                post("/auth/validate")
        ).andExpect(status().isBadRequest());

    }

    @Test
    void auth_ShouldRefreshToken_whenRefreshTokenPresent() throws Exception {

        AuthResponse authResponse = new AuthResponse("access", "refresh", "Bearer", 60, "test", 1L);

        when(cookieUtil.getRefreshTokenCookie(any())).thenReturn(Optional.of("refresh"));
        when(authService.refreshToken(any())).thenReturn(authResponse);

        mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(new Cookie("accessToken", "access"))
                                .cookie(new Cookie("refreshToken", "refresh"))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("test"));
    }

    @Test
    void auth_ShouldNotValidate_whenRefreshTokenMissing() throws Exception {

        mockMvc.perform(
                post("/auth/refresh")
        ).andExpect(status().is5xxServerError());
    }

    @Test
    void auth_ShouldLogout() throws Exception {
        mockMvc.perform(
                post("/auth/logout")
        ).andExpect(status().isOk());
    }

}
