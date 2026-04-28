package com.dukaanpe.supplierpurchase;

import com.dukaanpe.supplierpurchase.dto.PurchaseOrderItemRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderRequest;
import com.dukaanpe.supplierpurchase.dto.UpdatePurchaseOrderStatusRequest;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PurchaseOrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededPurchaseOrdersWithPaging() throws Exception {
        mockMvc.perform(get("/api/purchase-orders")
                .param("storeId", "1")
                .param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    void shouldCreatePurchaseOrderAndUpdateStatus() throws Exception {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setStoreId(1L);
        request.setSupplierId(1L);
        request.setOrderDate(LocalDate.now());
        request.setExpectedDeliveryDate(LocalDate.now().plusDays(2));
        request.setCreatedBy("integration-test");
        request.setItems(List.of(item("Fortune Oil", 20.0, "LITRE", "120.00", "5.00")));

        String response = mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalAmount").value(2520.00))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long id = objectMapper.readTree(response).path("data").path("id").asLong();

        UpdatePurchaseOrderStatusRequest statusRequest = new UpdatePurchaseOrderStatusRequest();
        statusRequest.setStatus(PurchaseOrderStatus.SENT);

        mockMvc.perform(put("/api/purchase-orders/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SENT"));
    }

    @Test
    void shouldCancelPurchaseOrder() throws Exception {
        mockMvc.perform(delete("/api/purchase-orders/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/purchase-orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void shouldReturnAutoSuggestFromHistoricalData() throws Exception {
        mockMvc.perform(get("/api/purchase-orders/auto-suggest")
                .param("storeId", "1")
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].totalItems").exists())
            .andExpect(jsonPath("$.data[0].items[0].suggestedQuantity").isNumber());
    }

    private PurchaseOrderItemRequest item(String name, Double qty, String unit, String price, String gstRate) {
        PurchaseOrderItemRequest item = new PurchaseOrderItemRequest();
        item.setProductName(name);
        item.setQuantityOrdered(qty);
        item.setUnit(unit);
        item.setUnitPrice(new BigDecimal(price));
        item.setGstRate(new BigDecimal(gstRate));
        return item;
    }
}

