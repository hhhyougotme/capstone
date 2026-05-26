package com.flashmart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "flashmart.auth")
public class AuthProperties {
    private String tokenHeader = "Authorization";
    private String tokenPrefix = "Bearer ";
    private String redisKeyPrefix = "flashmart:session:";
    private int tokenTtlHours = 168;
}
