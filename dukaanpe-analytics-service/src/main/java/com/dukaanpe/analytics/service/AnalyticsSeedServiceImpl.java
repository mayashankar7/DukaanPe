package com.dukaanpe.analytics.service;

import com.dukaanpe.analytics.dto.SeedAnalyticsResponse;
import com.dukaanpe.analytics.entity.DailyProductMetric;
import com.dukaanpe.analytics.entity.DailySalesMetric;
import com.dukaanpe.analytics.exception.InvalidAnalyticsOperationException;
import com.dukaanpe.analytics.repository.DailyProductMetricRepository;
import com.dukaanpe.analytics.repository.DailySalesMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalyticsSeedServiceImpl implements AnalyticsSeedService {

    private final DailySalesMetricRepository dailySalesMetricRepository;
    private final DailyProductMetricRepository dailyProductMetricRepository;

    @Override
    public SeedAnalyticsResponse seed(Long storeId, LocalDate fromDate, LocalDate toDate) {
        validateRange(fromDate, toDate);

        dailySalesMetricRepository.deleteByStoreIdAndMetricDateBetween(storeId, fromDate, toDate);
        dailyProductMetricRepository.deleteByStoreIdAndMetricDateBetween(storeId, fromDate, toDate);

        List<DailySalesMetric> salesRows = new ArrayList<>();
        List<DailyProductMetric> productRows = new ArrayList<>();

        LocalDate cursor = fromDate;
        while (!cursor.isAfter(toDate)) {
            int dayIndex = (int) ChronoUnit.DAYS.between(fromDate, cursor);
            SeedNumbers numbers = generateNumbers(storeId, cursor, dayIndex);

            salesRows.add(DailySalesMetric.builder()
                .storeId(storeId)
                .metricDate(cursor)
                .grossSales(numbers.grossSales)
                .netSales(numbers.netSales)
                .taxAmount(numbers.taxAmount)
                .ordersCount(numbers.orders)
                .build());

            productRows.addAll(generateProductRows(storeId, cursor, numbers.netSales));
            cursor = cursor.plusDays(1);
        }

        dailySalesMetricRepository.saveAll(salesRows);
        dailyProductMetricRepository.saveAll(productRows);

        return SeedAnalyticsResponse.builder()
            .storeId(storeId)
            .daysSeeded(salesRows.size())
            .productRowsSeeded(productRows.size())
            .build();
    }

    private List<DailyProductMetric> generateProductRows(Long storeId, LocalDate date, BigDecimal netSales) {
        BigDecimal p1 = scale(netSales.multiply(new BigDecimal("0.45")));
        BigDecimal p2 = scale(netSales.multiply(new BigDecimal("0.35")));
        BigDecimal p3 = scale(netSales.subtract(p1).subtract(p2));

        return List.of(
            DailyProductMetric.builder()
                .storeId(storeId)
                .metricDate(date)
                .productId(1001L)
                .productName("Aashirvaad Atta 10kg")
                .quantitySold(scale(p1.divide(new BigDecimal("500"), 3, RoundingMode.HALF_UP)))
                .revenue(p1)
                .build(),
            DailyProductMetric.builder()
                .storeId(storeId)
                .metricDate(date)
                .productId(1002L)
                .productName("Fortune Sunflower Oil 1L")
                .quantitySold(scale(p2.divide(new BigDecimal("150"), 3, RoundingMode.HALF_UP)))
                .revenue(p2)
                .build(),
            DailyProductMetric.builder()
                .storeId(storeId)
                .metricDate(date)
                .productId(1003L)
                .productName("Good Day Biscuits")
                .quantitySold(scale(p3.divide(new BigDecimal("20"), 3, RoundingMode.HALF_UP)))
                .revenue(p3)
                .build()
        );
    }

    private SeedNumbers generateNumbers(Long storeId, LocalDate date, int dayIndex) {
        int weekendBoost = isWeekend(date) ? 20 : 0;
        int trendBoost = dayIndex * 2;
        int storeOffset = (int) (storeId % 7) * 8;
        int orders = 65 + weekendBoost + trendBoost + storeOffset;

        BigDecimal averageOrder = new BigDecimal("135").add(new BigDecimal(storeOffset));
        BigDecimal netSales = scale(averageOrder.multiply(new BigDecimal(orders)));
        BigDecimal taxAmount = scale(netSales.multiply(new BigDecimal("0.05")));
        BigDecimal grossSales = scale(netSales.add(taxAmount));

        return new SeedNumbers(grossSales, netSales, taxAmount, orders);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private void validateRange(LocalDate fromDate, LocalDate toDate) {
        if (toDate.isBefore(fromDate)) {
            throw new InvalidAnalyticsOperationException("toDate must be on or after fromDate");
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record SeedNumbers(BigDecimal grossSales, BigDecimal netSales, BigDecimal taxAmount, Integer orders) {
    }
}

