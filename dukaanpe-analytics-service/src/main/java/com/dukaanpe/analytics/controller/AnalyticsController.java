package com.dukaanpe.analytics.controller;

import com.dukaanpe.analytics.dto.ApiResponse;
import com.dukaanpe.analytics.dto.DashboardResponse;
import com.dukaanpe.analytics.dto.ProductInsightsResponse;
import com.dukaanpe.analytics.dto.SalesInsightsResponse;
import com.dukaanpe.analytics.dto.SeedAnalyticsRequest;
import com.dukaanpe.analytics.dto.SeedAnalyticsResponse;
import com.dukaanpe.analytics.service.AnalyticsSeedService;
import com.dukaanpe.analytics.service.DashboardAnalyticsService;
import com.dukaanpe.analytics.service.ProductInsightsService;
import com.dukaanpe.analytics.service.SalesInsightsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private final AnalyticsSeedService analyticsSeedService;
    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final SalesInsightsService salesInsightsService;
    private final ProductInsightsService productInsightsService;

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<SeedAnalyticsResponse>> seed(@Valid @RequestBody SeedAnalyticsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Analytics time-series seeded",
                analyticsSeedService.seed(request.getStoreId(), request.getFromDate(), request.getToDate())));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(ApiResponse.success(dashboardAnalyticsService.getDashboard(storeId, fromDate, toDate)));
    }

    @GetMapping("/sales-insights")
    public ResponseEntity<ApiResponse<SalesInsightsResponse>> salesInsights(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(ApiResponse.success(salesInsightsService.getSalesInsights(storeId, fromDate, toDate)));
    }

    @GetMapping("/product-insights")
    public ResponseEntity<ApiResponse<ProductInsightsResponse>> productInsights(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "limit", defaultValue = "5") @Min(value = 1, message = "limit must be >= 1")
        @Max(value = 50, message = "limit must be <= 50") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(productInsightsService.getTopProducts(storeId, fromDate, toDate, limit)));
    }
}

