package com.dukaanpe.language.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslateResponse {

    private String sourceLanguage;
    private String targetLanguage;
    private String originalText;
    private String translatedText;
}

