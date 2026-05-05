package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.ProductInsightResponse;
import com.dukaanpe.analytics.dto.ProductInsightsResponse;
import com.dukaanpe.analytics.entity.DailyProductMetric;
import com.dukaanpe.analytics.exception.InvalidAnalyticsOperationException;
import com.dukaanpe.analytics.repository.DailyProductMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductInsightsServiceImpl implements ProductInsightsService {

    private final DailyProductMetricRepository dailyProductMetricRepository;

    @Override
    public ProductInsightsResponse getTopProducts(Long storeId, LocalDate fromDate, LocalDate toDate, int limit) {
        validateRange(fromDate, toDate);
        if (limit < 1 || limit > 50) {
            throw new InvalidAnalyticsOperationException("limit must be between 1 and 50");
        }

        List<DailyProductMetric> rows = dailyProductMetricRepository.findByStoreIdAndMetricDateBetween(storeId, fromDate, toDate);

        Map<Long, List<DailyProductMetric>> grouped = rows.stream()
            .collect(java.util.stream.Collectors.groupingBy(DailyProductMetric::getProductId));

        List<ProductInsightResponse> top = grouped.values().stream()
            .map(list -> {
                DailyProductMetric any = list.get(0);
                BigDecimal quantity = list.stream().map(DailyProductMetric::getQuantitySold).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal revenue = list.stream().map(DailyProductMetric::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
                return ProductInsightResponse.builder()
                    .productId(any.getProductId())
                    .productName(any.getProductName())
                    .totalQuantitySold(scale(quantity))
                    .totalRevenue(scale(revenue))
                    .build();
            })
            .sorted(Comparator.comparing(ProductInsightResponse::getTotalRevenue).reversed())
            .limit(limit)
            .toList();

        return ProductInsightsResponse.builder()
            .storeId(storeId)
            .fromDate(fromDate)
            .toDate(toDate)
            .limit(limit)
            .topProducts(top)
            .build();
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (toDate.isBefore(fromDate)) {
            throw new InvalidAnalyticsOperationException("toDate must be on or after fromDate");
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

