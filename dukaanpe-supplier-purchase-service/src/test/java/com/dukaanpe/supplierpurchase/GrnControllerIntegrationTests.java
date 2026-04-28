package com.dukaanpe.supplierpurchase;

import com.dukaanpe.supplierpurchase.dto.GrnItemRequest;
import com.dukaanpe.supplierpurchase.dto.InventoryAdjustHookRequest;
import com.dukaanpe.supplierpurchase.dto.GrnRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderItemRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GrnControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void shouldCreateAndApproveGrnAndUpdatePoStatus() throws Exception {
        when(restTemplate.postForEntity(any(String.class), any(), eq(Object.class))).thenReturn(null);

        Long poId = createPoForGrn();

        GrnRequest grnRequest = new GrnRequest();
        grnRequest.setStoreId(1L);
        grnRequest.setPurchaseOrderId(poId);
        grnRequest.setReceivedDate(LocalDate.now());
        grnRequest.setSupplierInvoiceNumber("INV-4455");
        grnRequest.setReceivedBy("warehouse-user");
        grnRequest.setItems(List.of(grnItem(9001L, "Test Refined Oil", 10.0, 7.0, "100.00")));

        String grnResponse = mockMvc.perform(post("/api/grn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(grnRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long grnId = objectMapper.readTree(grnResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/grn/{id}/approve", grnId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(put("/api/grn/{id}/verify", grnId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("VERIFIED"));

        mockMvc.perform(put("/api/grn/{id}/approve", grnId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(get("/api/purchase-orders/{id}", poId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PARTIALLY_RECEIVED"));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InventoryAdjustHookRequest> payloadCaptor = ArgumentCaptor.forClass(InventoryAdjustHookRequest.class);

        verify(restTemplate, times(1)).postForEntity(urlCaptor.capture(), payloadCaptor.capture(), eq(Object.class));

        org.junit.jupiter.api.Assertions.assertEquals("http://localhost:8083/api/inventory/adjust", urlCaptor.getValue());
        org.junit.jupiter.api.Assertions.assertEquals(9001L, payloadCaptor.getValue().getProductId());
        org.junit.jupiter.api.Assertions.assertEquals(1L, payloadCaptor.getValue().getStoreId());
        org.junit.jupiter.api.Assertions.assertEquals("PURCHASE", payloadCaptor.getValue().getTransactionType());
        org.junit.jupiter.api.Assertions.assertEquals(7.0, payloadCaptor.getValue().getQuantity());
    }

    @Test
    void shouldListGrnsByStore() throws Exception {
        mockMvc.perform(get("/api/grn").param("storeId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    private Long createPoForGrn() throws Exception {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setStoreId(1L);
        request.setSupplierId(1L);
        request.setOrderDate(LocalDate.now());
        request.setExpectedDeliveryDate(LocalDate.now().plusDays(2));
        request.setCreatedBy("grn-test");
        request.setItems(List.of(poItem("Test Refined Oil", 10.0, "LITRE", "100.00", "5.00")));

        String response = mockMvc.perform(post("/api/purchase-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }

    private PurchaseOrderItemRequest poItem(String name, Double qty, String unit, String price, String gstRate) {
        PurchaseOrderItemRequest item = new PurchaseOrderItemRequest();
        item.setProductName(name);
        item.setQuantityOrdered(qty);
        item.setUnit(unit);
        item.setUnitPrice(new BigDecimal(price));
        item.setGstRate(new BigDecimal(gstRate));
        return item;
    }

    private GrnItemRequest grnItem(Long productId, String name, Double received, Double accepted, String unitPrice) {
        GrnItemRequest item = new GrnItemRequest();
        item.setProductId(productId);
        item.setProductName(name);
        item.setQuantityReceived(received);
        item.setQuantityAccepted(accepted);
        item.setQuantityRejected(received - accepted);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }
}

