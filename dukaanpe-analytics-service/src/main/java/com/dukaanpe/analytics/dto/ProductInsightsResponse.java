package com.dukaanpe.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProductInsightsResponse {

    private Long storeId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int limit;
    private List<ProductInsightResponse> topProducts;
}

