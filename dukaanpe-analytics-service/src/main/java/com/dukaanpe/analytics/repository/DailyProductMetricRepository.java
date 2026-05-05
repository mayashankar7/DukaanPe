package com.dukaanpe.analytics.repository;

import com.dukaanpe.analytics.entity.DailyProductMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyProductMetricRepository extends JpaRepository<DailyProductMetric, Long> {

    List<DailyProductMetric> findByStoreIdAndMetricDateBetween(Long storeId, LocalDate fromDate, LocalDate toDate);

    void deleteByStoreIdAndMetricDateBetween(Long storeId, LocalDate fromDate, LocalDate toDate);
}

