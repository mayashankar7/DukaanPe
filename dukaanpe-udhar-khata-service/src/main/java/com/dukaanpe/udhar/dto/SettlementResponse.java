package com.dukaanpe.udhar.dto;

import com.dukaanpe.udhar.entity.SettlementMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementResponse {

    private Long id;
    private Long khataCustomerId;
    private String customerName;
    private Long storeId;
    private LocalDate settlementDate;
    private BigDecimal totalOutstandingBefore;
    private BigDecimal amountSettled;
    private BigDecimal discountGiven;
    private SettlementMode settlementMode;
    private String notes;
    private LocalDateTime createdAt;
}

