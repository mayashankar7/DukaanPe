package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CloseCashRegisterRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "actualCashInDrawer is required")
    private BigDecimal actualCashInDrawer;

    @NotBlank(message = "closedBy is required")
    private String closedBy;

    private BigDecimal totalCashPaid;

    @Size(max = 40, message = "terminalId must be <= 40 chars")
    private String terminalId;
}

