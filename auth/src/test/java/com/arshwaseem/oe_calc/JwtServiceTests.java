package com.arshwaseem.oe_calc;

import com.arshwaseem.oe_calc.DTOs.UserDTO;
import com.arshwaseem.oe_calc.configuration.JwtProperties;
import io.jsonwebtoken.JwtException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class JwtServiceTests {

    private JwtService jwtService;

    private final String secretKey = "Aw7PTgR6oOahlwGLqgtWfV1TUKN61BwW";

    @BeforeEach
    void setUp(){

        long expiration = 3600000;

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(secretKey);
        jwtProperties.setExpiration(expiration);
        jwtProperties.setRefreshExpiration(expiration);
        this.jwtService = new JwtService(jwtProperties);
    }

    @Test
    void auth_ShouldGenerateValidToken(){

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("test");
        userDTO.setPassword("pass");

        String token = jwtService.generateToken(userDTO);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.contains("."));
        Assertions.assertEquals(3, token.split("\\.").length);

    }

    @Test
    void auth_ValidTokenShouldReturnTrue(){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("test");
        userDTO.setPassword("pass");

        String token = jwtService.generateToken(userDTO);

        boolean valid = jwtService.validateToken(token);

        Assertions.assertTrue(valid);
    }

    @Test
    void auth_TamperedTokenShouldReturnFalse(){

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("test");
        userDTO.setPassword("pass");

        String token = jwtService.generateToken(userDTO);

        String tamperedToken = token.substring(0, token.length()-5)+"XXXXX";

        boolean valid = jwtService.validateToken(tamperedToken);

        Assertions.assertFalse(valid);
    }

    @Test
    void auth_ExpiredTokenShouldReturnFalse(){

        JwtProperties shortExpirationJwt = new JwtProperties();

        shortExpirationJwt.setExpiration(1);
        shortExpirationJwt.setRefreshExpiration(1);
        shortExpirationJwt.setSecret(secretKey);

        JwtService shortLivedJwt = new JwtService(shortExpirationJwt);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("test");
        userDTO.setPassword("pass");

        String token = shortLivedJwt.generateToken(userDTO);

        Awaitility.await()
                .pollDelay(10, TimeUnit.MILLISECONDS)
                .atMost(1, TimeUnit.SECONDS)
                .until(()->true);

        boolean valid = shortLivedJwt.validateToken(token);
        Assertions.assertFalse(valid);

    }

    @Test
    void auth_ShouldExtractCorrectUsername(){

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("test");
        userDTO.setPassword("pass");

        String token = jwtService.generateToken(userDTO);

        String username = jwtService.extractUsername(token);

        Assertions.assertEquals(username, userDTO.getUsername());

    }

    @Test
    void auth_ShouldThrowExceptionWhenExtractingClaimFromMalformedToken(){

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("test");
        userDTO.setPassword("pass");

        String token = jwtService.generateToken(userDTO);

        String malformedToken = token.substring(0, token.length()-5)+"XXXXX";

        Assertions.assertThrows(JwtException.class, ()->{
            jwtService.extractUsername(malformedToken);
        });

    }
}
