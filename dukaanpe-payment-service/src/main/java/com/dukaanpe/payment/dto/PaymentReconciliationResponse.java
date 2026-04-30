package com.dukaanpe.payment.dto;

import com.dukaanpe.payment.entity.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentReconciliationResponse {

    private Long id;
    private Long storeId;
    private LocalDate reconDate;
    private BigDecimal totalCash;
    private BigDecimal totalUpi;
    private BigDecimal totalCard;
    private BigDecimal totalCollections;
    private BigDecimal cashInHand;
    private BigDecimal discrepancy;
    private ReconciliationStatus status;
    private String notes;
    private LocalDateTime createdAt;
}

