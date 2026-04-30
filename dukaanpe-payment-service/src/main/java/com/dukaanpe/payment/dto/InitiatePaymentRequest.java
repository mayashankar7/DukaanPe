package com.dukaanpe.payment.dto;

import com.dukaanpe.payment.entity.PaymentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class InitiatePaymentRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @Positive(message = "billId must be positive")
    private Long billId;

    @Positive(message = "udharEntryId must be positive")
    private Long udharEntryId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "paymentMode is required")
    private PaymentMode paymentMode;

    @Size(max = 120, message = "upiId must be <= 120 chars")
    private String upiId;

    @Size(max = 120, message = "payerName must be <= 120 chars")
    private String payerName;

    @Pattern(regexp = "^$|^[6-9][0-9]{9}$", message = "payerPhone must be a valid 10-digit Indian mobile")
    private String payerPhone;

    @Size(max = 500, message = "description must be <= 500 chars")
    private String description;

    @Size(max = 40, message = "terminalId must be <= 40 chars")
    private String terminalId;
}

