package com.dukaanpe.customerloyalty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RecordPurchaseRequest {

    @NotNull(message = "billId is required")
    @Positive(message = "billId must be positive")
    private Long billId;

    @NotBlank(message = "billNumber is required")
    @Size(max = 60, message = "billNumber must be <= 60 chars")
    private String billNumber;

    @NotNull(message = "billDate is required")
    private LocalDate billDate;

    @NotNull(message = "totalAmount is required")
    @Positive(message = "totalAmount must be positive")
    private BigDecimal totalAmount;

    @Size(max = 500, message = "itemsSummary must be <= 500 chars")
    private String itemsSummary;

    @Size(max = 40, message = "paymentMode must be <= 40 chars")
    private String paymentMode;
}

