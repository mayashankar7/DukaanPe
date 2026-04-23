package com.dukaanpe.auth.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSessionResponse {

    private String id;
    private String deviceName;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime lastActiveAt;
    private boolean current;
}

