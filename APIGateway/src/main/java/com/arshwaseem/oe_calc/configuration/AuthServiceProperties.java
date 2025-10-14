package com.arshwaseem.oe_calc.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AuthServiceProperties {
    @Value("${auth.service.url}")
    private String url;
}
