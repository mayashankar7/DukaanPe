package com.dukaanpe.analytics.repository;

import com.dukaanpe.analytics.entity.DailySalesMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailySalesMetricRepository extends JpaRepository<DailySalesMetric, Long> {

    List<DailySalesMetric> findByStoreIdAndMetricDateBetweenOrderByMetricDateAsc(Long storeId, LocalDate fromDate, LocalDate toDate);

    void deleteByStoreIdAndMetricDateBetween(Long storeId, LocalDate fromDate, LocalDate toDate);
}

