package com.dukaanpe.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.inventory.dto.StockAdjustmentRequest;
import com.dukaanpe.inventory.entity.InventoryTransactionType;
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
class InventoryControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRecordAndFetchTransactionsForProduct() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setProductId(1L);
        request.setStoreId(1L);
        request.setTransactionType(InventoryTransactionType.PURCHASE);
        request.setQuantity(3.0);
        request.setReferenceId("PO-TEST-001");
        request.setNotes("Integration test purchase");
        request.setCreatedBy("test-user");

        mockMvc.perform(post("/api/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/inventory/transactions")
                .param("storeId", "1")
                .param("productId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").isNumber())
            .andExpect(jsonPath("$.data[0].transactionType").value("PURCHASE"));
    }

    @Test
    void shouldRejectInvalidTransactionsFilter() throws Exception {
        mockMvc.perform(get("/api/inventory/transactions")
                .param("storeId", "1")
                .param("productId", "-2"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}

