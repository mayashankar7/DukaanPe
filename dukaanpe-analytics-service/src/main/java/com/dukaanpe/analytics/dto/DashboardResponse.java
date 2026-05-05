package com.dukaanpe.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private Long storeId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long days;
    private long totalOrders;
    private BigDecimal grossSales;
    private BigDecimal netSales;
    private BigDecimal totalTax;
    private BigDecimal averageOrderValue;
    private List<DailySalesPointResponse> timeSeries;
}

