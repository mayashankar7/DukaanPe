package com.dukaanpe.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeedAnalyticsResponse {

    private Long storeId;
    private int daysSeeded;
    private int productRowsSeeded;
}

