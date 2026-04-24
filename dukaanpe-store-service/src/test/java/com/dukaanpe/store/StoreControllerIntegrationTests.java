package com.dukaanpe.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.store.dto.StoreRequest;
import com.dukaanpe.store.dto.UpdateSubscriptionRequest;
import com.dukaanpe.store.entity.BusinessCategory;
import com.dukaanpe.store.entity.SubscriptionPlan;
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
class StoreControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListSeededStores() throws Exception {
        mockMvc.perform(get("/api/stores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldCreateStore() throws Exception {
        StoreRequest request = new StoreRequest();
        request.setOwnerPhone("9876543299");
        request.setStoreName("Ganesh Kirana");
        request.setBusinessCategory(BusinessCategory.GENERAL);
        request.setCity("Nashik");
        request.setState("Maharashtra");
        request.setPincode("422001");
        request.setPhone("9876543299");

        mockMvc.perform(post("/api/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.storeName").value("Ganesh Kirana"))
            .andExpect(jsonPath("$.data.subscriptionPlan").value("FREE"));
    }

    @Test
    void shouldRejectInvalidCreatePayload() throws Exception {
        StoreRequest request = new StoreRequest();
        request.setOwnerPhone("123");
        request.setStoreName("");

        mockMvc.perform(post("/api/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldUpdateSubscription() throws Exception {
        UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setSubscriptionPlan(SubscriptionPlan.PREMIUM);

        mockMvc.perform(put("/api/stores/1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.subscriptionPlan").value("PREMIUM"));
    }
}

