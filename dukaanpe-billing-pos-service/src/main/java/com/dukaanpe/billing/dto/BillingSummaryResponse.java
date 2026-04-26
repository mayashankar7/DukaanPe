package com.dukaanpe.billing.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingSummaryResponse {

    private Long storeId;
    private String date;
    private long totalBills;
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private BigDecimal totalDiscount;
    private BigDecimal averageBillValue;
}

