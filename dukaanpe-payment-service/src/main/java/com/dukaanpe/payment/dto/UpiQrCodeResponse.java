package com.dukaanpe.payment.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpiQrCodeResponse {

    private Long id;
    private Long storeId;
    private String merchantUpiId;
    private String merchantName;
    private String qrCodeImageBase64;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}

