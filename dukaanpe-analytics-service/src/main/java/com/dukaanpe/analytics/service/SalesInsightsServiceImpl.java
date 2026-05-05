package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.SalesInsightsResponse;
import com.dukaanpe.analytics.entity.DailySalesMetric;
import com.dukaanpe.analytics.exception.InvalidAnalyticsOperationException;
import com.dukaanpe.analytics.repository.DailySalesMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesInsightsServiceImpl implements SalesInsightsService {

    private final DailySalesMetricRepository dailySalesMetricRepository;

    @Override
    public SalesInsightsResponse getSalesInsights(Long storeId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);

        List<DailySalesMetric> currentRows = dailySalesMetricRepository
            .findByStoreIdAndMetricDateBetweenOrderByMetricDateAsc(storeId, fromDate, toDate);

        long dayCount = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        LocalDate prevFrom = fromDate.minusDays(dayCount);
        LocalDate prevTo = toDate.minusDays(dayCount);
        List<DailySalesMetric> previousRows = dailySalesMetricRepository
            .findByStoreIdAndMetricDateBetweenOrderByMetricDateAsc(storeId, prevFrom, prevTo);

        BigDecimal currentNet = sumNet(currentRows);
        BigDecimal previousNet = sumNet(previousRows);
        BigDecimal growth = previousNet.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : scale(currentNet.subtract(previousNet)
                .multiply(new BigDecimal("100"))
                .divide(previousNet, 2, RoundingMode.HALF_UP));

        long orders = currentRows.stream().mapToLong(DailySalesMetric::getOrdersCount).sum();
        DailySalesMetric best = currentRows.stream()
            .max(Comparator.comparing(DailySalesMetric::getNetSales))
            .orElse(null);

        return SalesInsightsResponse.builder()
            .storeId(storeId)
            .fromDate(fromDate)
            .toDate(toDate)
            .currentPeriodNetSales(scale(currentNet))
            .previousPeriodNetSales(scale(previousNet))
            .growthPercent(growth)
            .averageDailyNetSales(dayCount == 0 ? BigDecimal.ZERO : scale(currentNet.divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP)))
            .averageOrderValue(orders == 0 ? BigDecimal.ZERO : scale(currentNet.divide(new BigDecimal(orders), 2, RoundingMode.HALF_UP)))
            .bestSalesDay(best == null ? null : best.getMetricDate())
            .bestSalesDayNetSales(best == null ? BigDecimal.ZERO : scale(best.getNetSales()))
            .build();
    }

    private BigDecimal sumNet(List<DailySalesMetric> rows) {
        return rows.stream().map(DailySalesMetric::getNetSales).reduce(BigDecimal.ZERO, BigDecimal::add);
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

