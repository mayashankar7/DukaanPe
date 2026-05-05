package com.dukaanpe.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.analytics.dto.SeedAnalyticsRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSeedAndFetchDashboardAndSalesInsights() throws Exception {
        LocalDate fromDate = LocalDate.now().minusDays(6);
        LocalDate toDate = LocalDate.now();
        seedStore(501L, fromDate, toDate);

        mockMvc.perform(get("/api/analytics/dashboard")
                .param("storeId", "501")
                .param("fromDate", fromDate.toString())
                .param("toDate", toDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.storeId").value(501))
            .andExpect(jsonPath("$.data.days").value(7))
            .andExpect(jsonPath("$.data.timeSeries.length()").value(7))
            .andExpect(jsonPath("$.data.netSales").isNumber())
            .andExpect(jsonPath("$.data.averageOrderValue").isNumber());

        mockMvc.perform(get("/api/analytics/sales-insights")
                .param("storeId", "501")
                .param("fromDate", fromDate.toString())
                .param("toDate", toDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.currentPeriodNetSales").isNumber())
            .andExpect(jsonPath("$.data.previousPeriodNetSales").isNumber())
            .andExpect(jsonPath("$.data.growthPercent").isNumber())
            .andExpect(jsonPath("$.data.bestSalesDay").isString());
    }

    @Test
    void shouldFetchProductInsightsSortedByRevenue() throws Exception {
        LocalDate fromDate = LocalDate.now().minusDays(9);
        LocalDate toDate = LocalDate.now();
        seedStore(502L, fromDate, toDate);

        mockMvc.perform(get("/api/analytics/product-insights")
                .param("storeId", "502")
                .param("fromDate", fromDate.toString())
                .param("toDate", toDate.toString())
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.limit").value(2))
            .andExpect(jsonPath("$.data.topProducts.length()").value(2))
            .andExpect(jsonPath("$.data.topProducts[0].totalRevenue").isNumber())
            .andExpect(jsonPath("$.data.topProducts[1].totalRevenue").isNumber())
            .andExpect(jsonPath("$.data.topProducts[0].totalRevenue")
                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(0.0)));
    }

    @Test
    void shouldValidateRangeAndLimitInputs() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard")
                .param("storeId", "503")
                .param("fromDate", LocalDate.now().toString())
                .param("toDate", LocalDate.now().minusDays(1).toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/analytics/product-insights")
                .param("storeId", "503")
                .param("fromDate", LocalDate.now().minusDays(2).toString())
                .param("toDate", LocalDate.now().toString())
                .param("limit", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    private void seedStore(Long storeId, LocalDate fromDate, LocalDate toDate) throws Exception {
        SeedAnalyticsRequest request = new SeedAnalyticsRequest();
        request.setStoreId(storeId);
        request.setFromDate(fromDate);
        request.setToDate(toDate);

        mockMvc.perform(post("/api/analytics/seed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.storeId").value(storeId));
    }
}

