package com.dukaanpe.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashRegisterResponse {

    private Long id;
    private Long storeId;
    private String terminalId;
    private LocalDate registerDate;
    private BigDecimal openingBalance;
    private BigDecimal totalCashReceived;
    private BigDecimal totalCashPaid;
    private BigDecimal closingBalance;
    private BigDecimal actualCashInDrawer;
    private BigDecimal difference;
    private String closedBy;
    private Boolean isClosed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

