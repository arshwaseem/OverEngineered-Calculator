package com.arshwaseem.oe_calc.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CookieProperties {
    private boolean httpOnly = true;
    private boolean secure = false;
    private String sameSite = "Lax";
    @Value("${cookie.domain:}")
    private String domain;
    @Value("${cookie.max.age:86400}")
    private int maxAge;
    @Value("${cookie.refresh.interval:604800}")
    private int refreshInterval;

}
