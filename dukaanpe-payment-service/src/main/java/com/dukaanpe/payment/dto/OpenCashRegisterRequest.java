package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class OpenCashRegisterRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "registerDate is required")
    private LocalDate registerDate;

    @NotNull(message = "openingBalance is required")
    @Positive(message = "openingBalance must be positive")
    private BigDecimal openingBalance;

    @Size(max = 40, message = "terminalId must be <= 40 chars")
    private String terminalId;
}

