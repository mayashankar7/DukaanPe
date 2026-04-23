package com.dukaanpe.auth.config;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@Slf4j
public class JwtConfig {

    private final Environment environment;

    public JwtConfig(Environment environment) {
        this.environment = environment;
    }

    private String secret;
    private Long expiration;
    private Long refreshExpiration;
    private Boolean allowFixedOtp = false;

    @PostConstruct
    void enforceFixedOtpOnlyInDev() {
        if (!Boolean.TRUE.equals(allowFixedOtp)) {
            return;
        }

        boolean isAllowedProfileActive = Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> "dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile));
        if (!isAllowedProfileActive) {
            log.warn("jwt.allow-fixed-otp=true ignored because active profile is neither dev nor test.");
            allowFixedOtp = false;
        }
    }
}

