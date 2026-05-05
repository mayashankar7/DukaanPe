package com.dukaanpe.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalesInsightsResponse {

    private Long storeId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal currentPeriodNetSales;
    private BigDecimal previousPeriodNetSales;
    private BigDecimal growthPercent;
    private BigDecimal averageDailyNetSales;
    private BigDecimal averageOrderValue;
    private LocalDate bestSalesDay;
    private BigDecimal bestSalesDayNetSales;
}

