package com.dukaanpe.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductInsightResponse {

    private Long productId;
    private String productName;
    private BigDecimal totalQuantitySold;
    private BigDecimal totalRevenue;
}

