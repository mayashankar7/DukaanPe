package com.dukaanpe.customerloyalty.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseHistoryResponse {

    private Long id;
    private Long customerId;
    private Long storeId;
    private Long billId;
    private String billNumber;
    private LocalDate billDate;
    private BigDecimal totalAmount;
    private String itemsSummary;
    private String paymentMode;
}

