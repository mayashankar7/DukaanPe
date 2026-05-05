package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.DashboardResponse;

import java.time.LocalDate;

public interface DashboardAnalyticsService {

    DashboardResponse getDashboard(Long storeId, LocalDate fromDate, LocalDate toDate);
}

