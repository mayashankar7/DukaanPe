package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.ProductInsightsResponse;

import java.time.LocalDate;

public interface ProductInsightsService {

    ProductInsightsResponse getTopProducts(Long storeId, LocalDate fromDate, LocalDate toDate, int limit);
}

