package com.dukaanpe.supplierpurchase;

import com.dukaanpe.supplierpurchase.dto.SupplierRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupplierControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededSuppliersByStore() throws Exception {
        mockMvc.perform(get("/api/suppliers").param("storeId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldCreateSupplier() throws Exception {
        SupplierRequest request = new SupplierRequest();
        request.setStoreId(1L);
        request.setSupplierName("Metro Snacks Distributor");
        request.setPhone("9876500099");
        request.setCity("Pune");
        request.setState("Maharashtra");
        request.setRating(4);

        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.supplierName").value("Metro Snacks Distributor"));
    }

    @Test
    void shouldValidateCreateSupplierPayload() throws Exception {
        SupplierRequest request = new SupplierRequest();
        request.setStoreId(1L);
        request.setSupplierName(" ");
        request.setPhone("12345");

        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldUpdateAndDeactivateSupplier() throws Exception {
        SupplierRequest updateRequest = new SupplierRequest();
        updateRequest.setStoreId(1L);
        updateRequest.setSupplierName("Pune Grocery Hub");
        updateRequest.setPhone("9876500011");
        updateRequest.setPaymentTerms("Net 21");

        mockMvc.perform(put("/api/suppliers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentTerms").value("Net 21"));

        mockMvc.perform(delete("/api/suppliers/1"))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/suppliers/1"))
            .andExpect(status().isNotFound());
    }
}

