package com.dukaanpe.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailySalesPointResponse {

    private LocalDate date;
    private BigDecimal grossSales;
    private BigDecimal netSales;
    private BigDecimal taxAmount;
    private Integer ordersCount;
}

