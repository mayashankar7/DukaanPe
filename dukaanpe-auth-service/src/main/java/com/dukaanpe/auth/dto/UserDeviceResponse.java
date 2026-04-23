package com.dukaanpe.auth.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDeviceResponse {

    private String id;
    private String deviceName;
    private String platform;
    private String appVersion;
    private LocalDateTime lastSeenAt;
    private boolean trusted;
}

