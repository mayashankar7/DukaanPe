package com.dukaanpe.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.billing.dto.BillItemRequest;
import com.dukaanpe.billing.dto.CreateBillRequest;
import com.dukaanpe.billing.entity.PaymentMode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BillingControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateBillAndFetchIt() throws Exception {
        CreateBillRequest request = new CreateBillRequest();
        request.setStoreId(1L);
        request.setPaymentMode(PaymentMode.CASH);
        request.setCustomerName("Test Customer");
        request.setCustomerPhone("9876543219");
        request.setItems(List.of(item(201L, "Test Item", 2.0, 40)));
        request.setCashAmount(new BigDecimal("100"));

        String response = mockMvc.perform(post("/api/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn().getResponse().getContentAsString();

        String billNumber = objectMapper.readTree(response).path("data").path("billNumber").asText();

        mockMvc.perform(get("/api/bills/number/{billNumber}", billNumber))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.billNumber").value(billNumber));
    }

    @Test
    void shouldCalculatePosPreview() throws Exception {
        mockMvc.perform(post("/api/pos/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"productId\":1,\"productName\":\"Milk\",\"quantity\":2,\"unit\":\"PIECE\",\"unitPrice\":27,\"mrp\":29,\"discountPercent\":0,\"gstRate\":5}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.grandTotal").isNumber());
    }

    @Test
    void shouldCancelBill() throws Exception {
        mockMvc.perform(put("/api/bills/1/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void shouldListBillsWithDateAndPaginationEdges() throws Exception {
        Long storeId = 91L;
        createBill(storeId, "List Edge A", null);
        createBill(storeId, "List Edge B", null);
        createBill(storeId, "List Edge C", null);

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/bills")
                .param("storeId", storeId.toString())
                .param("date", today)
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pageNumber").value(0))
            .andExpect(jsonPath("$.data.pageSize").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.content.length()").value(2));

        mockMvc.perform(get("/api/bills")
                .param("storeId", storeId.toString())
                .param("date", today)
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pageNumber").value(1))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.last").value(true));

        mockMvc.perform(get("/api/bills")
                .param("storeId", storeId.toString())
                .param("date", "2000-01-01")
                .param("page", "0")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void shouldValidateListPaginationEdges() throws Exception {
        mockMvc.perform(get("/api/bills")
                .param("storeId", "1")
                .param("page", "-1")
                .param("size", "20"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/bills")
                .param("storeId", "1")
                .param("page", "0")
                .param("size", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldSearchBillsWithQAndPaginationEdges() throws Exception {
        Long storeId = 92L;
        String query = "EdgeQ-" + System.currentTimeMillis();

        createBill(storeId, query + "-A", "9876543205");
        createBill(storeId, query + "-B", "9876543206");
        createBill(storeId, query + "-C", "9876543207");

        mockMvc.perform(get("/api/bills/search")
                .param("storeId", storeId.toString())
                .param("q", query)
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.pageNumber").value(0))
            .andExpect(jsonPath("$.data.pageSize").value(2))
            .andExpect(jsonPath("$.data.totalElements").value(3))
            .andExpect(jsonPath("$.data.content.length()").value(2));

        mockMvc.perform(get("/api/bills/search")
                .param("storeId", storeId.toString())
                .param("q", query)
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pageNumber").value(1))
            .andExpect(jsonPath("$.data.content.length()").value(1));

        mockMvc.perform(get("/api/bills/search")
                .param("storeId", storeId.toString())
                .param("q", "no-match-" + query)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    @Test
    void shouldValidateSearchPaginationAndRequiredQuery() throws Exception {
        mockMvc.perform(get("/api/bills/search")
                .param("storeId", "1")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/bills/search")
                .param("storeId", "1")
                .param("q", "RGS")
                .param("page", "0")
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldCreateCreditBillWhenUdharHookDisabledInTests() throws Exception {
        CreateBillRequest request = new CreateBillRequest();
        request.setStoreId(93L);
        request.setPaymentMode(PaymentMode.CREDIT);
        request.setCustomerName("Credit Test Customer");
        request.setCustomerPhone("9876543291");
        request.setItems(List.of(item(401L, "Credit Item", 1.0, 120)));
        request.setCreditAmount(new BigDecimal("126"));

        mockMvc.perform(post("/api/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.paymentMode").value("CREDIT"))
            .andExpect(jsonPath("$.data.isCredit").value(true));
    }

    private void createBill(Long storeId, String customerName, String customerPhone) throws Exception {
        CreateBillRequest request = new CreateBillRequest();
        request.setStoreId(storeId);
        request.setPaymentMode(PaymentMode.CASH);
        request.setCustomerName(customerName);
        request.setCustomerPhone(customerPhone);
        request.setItems(List.of(item(301L, "Edge Item", 1.0, 40)));
        request.setCashAmount(new BigDecimal("100"));

        mockMvc.perform(post("/api/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    private BillItemRequest item(Long id, String name, Double qty, int price) {
        BillItemRequest item = new BillItemRequest();
        item.setProductId(id);
        item.setProductName(name);
        item.setQuantity(qty);
        item.setUnit("PIECE");
        item.setMrp(BigDecimal.valueOf(price));
        item.setUnitPrice(BigDecimal.valueOf(price));
        item.setDiscountPercent(BigDecimal.ZERO);
        item.setGstRate(new BigDecimal("5"));
        return item;
    }
}

