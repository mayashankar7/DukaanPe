package com.dukaanpe.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.notification.dto.NotificationRequest;
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
class NotificationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSendAndListNotifications() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setStoreId(601L);
        request.setChannel("sms");
        request.setRecipient("9876500001");
        request.setTitle("Payment Received");
        request.setMessage("You received Rs 500");

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("SENT"));

        mockMvc.perform(get("/api/notifications")
                .param("storeId", "601"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].recipient").value("9876500001"));
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldMarkUnsupportedChannelAsFailed() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setStoreId(602L);
        request.setChannel("fax");
        request.setRecipient("9876500002");
        request.setTitle("Unsupported");
        request.setMessage("will fail");

        mockMvc.perform(post("/api/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("FAILED"));
    }
}

