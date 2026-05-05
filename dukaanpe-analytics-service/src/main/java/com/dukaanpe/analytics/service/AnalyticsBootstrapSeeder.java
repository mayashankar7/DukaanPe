package com.dukaanpe.analytics.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsBootstrapSeeder implements CommandLineRunner {

    private final AnalyticsSeedService analyticsSeedService;

    @Value("${analytics.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(29);

        analyticsSeedService.seed(201L, fromDate, toDate);
        analyticsSeedService.seed(202L, fromDate, toDate);
        log.info("Analytics seed completed for stores 201 and 202 from {} to {}", fromDate, toDate);
    }
}

