package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.DailySalesPointResponse;
import com.dukaanpe.analytics.dto.DashboardResponse;
import com.dukaanpe.analytics.entity.DailySalesMetric;
import com.dukaanpe.analytics.exception.InvalidAnalyticsOperationException;
import com.dukaanpe.analytics.repository.DailySalesMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {

    private final DailySalesMetricRepository dailySalesMetricRepository;

    @Override
    public DashboardResponse getDashboard(Long storeId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);

        List<DailySalesMetric> rows = dailySalesMetricRepository
            .findByStoreIdAndMetricDateBetweenOrderByMetricDateAsc(storeId, fromDate, toDate);

        BigDecimal gross = rows.stream().map(DailySalesMetric::getGrossSales).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = rows.stream().map(DailySalesMetric::getNetSales).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = rows.stream().map(DailySalesMetric::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long orders = rows.stream().mapToLong(DailySalesMetric::getOrdersCount).sum();
        long days = ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        List<DailySalesPointResponse> timeSeries = rows.stream().map(row -> DailySalesPointResponse.builder()
            .date(row.getMetricDate())
            .grossSales(row.getGrossSales())
            .netSales(row.getNetSales())
            .taxAmount(row.getTaxAmount())
            .ordersCount(row.getOrdersCount())
            .build()).toList();

        return DashboardResponse.builder()
            .storeId(storeId)
            .fromDate(fromDate)
            .toDate(toDate)
            .days(days)
            .totalOrders(orders)
            .grossSales(scale(gross))
            .netSales(scale(net))
            .totalTax(scale(tax))
            .averageOrderValue(orders == 0 ? BigDecimal.ZERO : scale(net.divide(new BigDecimal(orders), 2, RoundingMode.HALF_UP)))
            .timeSeries(timeSeries)
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

