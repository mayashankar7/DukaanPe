package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.SeedAnalyticsResponse;

import java.time.LocalDate;

public interface AnalyticsSeedService {

    SeedAnalyticsResponse seed(Long storeId, LocalDate fromDate, LocalDate toDate);
}

