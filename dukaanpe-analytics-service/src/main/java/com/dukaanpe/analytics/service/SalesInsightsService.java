package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.SalesInsightsResponse;

import java.time.LocalDate;

public interface SalesInsightsService {

    SalesInsightsResponse getSalesInsights(Long storeId, LocalDate fromDate, LocalDate toDate);
}

