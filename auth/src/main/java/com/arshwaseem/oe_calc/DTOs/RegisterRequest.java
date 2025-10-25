package com.arshwaseem.oe_calc.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 25, message = "Username must be between 5 and 25 characters")
    @Pattern(regexp = "^[A-za-z0-9]+$", message = "Username can only contain numbers and alphabets")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
