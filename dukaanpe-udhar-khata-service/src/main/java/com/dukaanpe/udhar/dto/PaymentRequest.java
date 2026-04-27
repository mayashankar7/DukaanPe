package com.dukaanpe.udhar.dto;

import com.dukaanpe.udhar.entity.UdharPaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "khataCustomerId is required")
    @Positive(message = "khataCustomerId must be positive")
    private Long khataCustomerId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "paymentMode is required")
    private UdharPaymentMode paymentMode;

    private String referenceNumber;
    private String description;
    private String createdBy;
}

