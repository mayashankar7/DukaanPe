package com.dukaanpe.language;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dukaanpe.language.dto.TranslateRequest;
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
class LanguageControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldTranslateAndListSupportedLanguages() throws Exception {
        TranslateRequest request = new TranslateRequest();
        request.setText("hello");
        request.setSourceLanguage("en");
        request.setTargetLanguage("hi");

        mockMvc.perform(post("/api/language/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.translatedText").value("namaste"));

        mockMvc.perform(get("/api/language/supported"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(6));
    }

    @Test
    void shouldValidateTranslateRequest() throws Exception {
        mockMvc.perform(post("/api/language/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldRejectUnsupportedLanguageCode() throws Exception {
        TranslateRequest request = new TranslateRequest();
        request.setText("hello");
        request.setSourceLanguage("en");
        request.setTargetLanguage("xx");

        mockMvc.perform(post("/api/language/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("targetLanguage is not supported")));
    }
}

