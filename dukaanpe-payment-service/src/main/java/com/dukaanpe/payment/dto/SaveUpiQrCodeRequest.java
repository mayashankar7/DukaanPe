package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveUpiQrCodeRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotBlank(message = "merchantUpiId is required")
    @Size(max = 120, message = "merchantUpiId must be <= 120 chars")
    private String merchantUpiId;

    @NotBlank(message = "merchantName is required")
    @Size(max = 120, message = "merchantName must be <= 120 chars")
    private String merchantName;

    @Size(max = 4000, message = "qrCodeImageBase64 must be <= 4000 chars")
    private String qrCodeImageBase64;

    private Boolean isDefault;
}

