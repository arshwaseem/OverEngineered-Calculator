package com.arshwaseem.oe_calc.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username Is Required")
    private String username;
    @NotBlank(message = "Password Is Required")
    private String password;
}
