package com.dukaanpe.supplierpurchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GrnItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Double quantityReceived;
    private Double quantityAccepted;
    private Double quantityRejected;
    private String rejectionReason;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}

