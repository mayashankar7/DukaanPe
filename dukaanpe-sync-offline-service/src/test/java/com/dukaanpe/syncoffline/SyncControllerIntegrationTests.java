package com.dukaanpe.syncoffline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.syncoffline.dto.SyncPushRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SyncControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPushAndPullSyncEvents() throws Exception {
        SyncPushRequest request = new SyncPushRequest();
        request.setStoreId(701L);
        request.setEntityType("BILL");
        request.setEntityId("BILL-1001");
        request.setOperation("UPSERT");
        request.setPayload("{\"amount\":1200}");

        mockMvc.perform(post("/api/sync/push")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.entityId").value("BILL-1001"));

        mockMvc.perform(get("/api/sync/pull")
                .param("storeId", "701")
                .param("since", LocalDateTime.now().minusMinutes(2).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].operation").value("UPSERT"));
    }

    @Test
    void shouldValidatePushInput() throws Exception {
        mockMvc.perform(post("/api/sync/push")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldRejectUnsupportedOperation() throws Exception {
        SyncPushRequest request = new SyncPushRequest();
        request.setStoreId(702L);
        request.setEntityType("PRODUCT");
        request.setEntityId("P-1");
        request.setOperation("INVALID");
        request.setPayload("{}");

        mockMvc.perform(post("/api/sync/push")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("operation must be one of")));
    }

    @Test
    void shouldDeduplicateSamePushWithinShortWindow() throws Exception {
        SyncPushRequest request = new SyncPushRequest();
        request.setStoreId(703L);
        request.setEntityType("BILL");
        request.setEntityId("BILL-2001");
        request.setOperation("UPSERT");
        request.setPayload("{\"amount\":500}");

        String body = objectMapper.writeValueAsString(request);
        String first = mockMvc.perform(post("/api/sync/push")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String second = mockMvc.perform(post("/api/sync/push")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long firstId = objectMapper.readTree(first).path("data").path("id").asLong();
        Long secondId = objectMapper.readTree(second).path("data").path("id").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(firstId, secondId);
    }
}

