package com.dukaanpe.language.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TranslateRequest {

    @NotBlank(message = "text is required")
    private String text;

    @NotBlank(message = "sourceLanguage is required")
    private String sourceLanguage;

    @NotBlank(message = "targetLanguage is required")
    private String targetLanguage;
}

