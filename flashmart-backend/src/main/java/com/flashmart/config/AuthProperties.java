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
    private String userSessionPrefix = "flashmart:user-session:";
    private String verificationPrefix = "flashmart:verify:";
    private int tokenTtlHours = 168;
    private int verificationCodeTtlMinutes = 5;
    /** Demo only: return code in API response so testers need no SMS gateway. */
    private boolean exposeVerificationCodeInResponse = true;
}
