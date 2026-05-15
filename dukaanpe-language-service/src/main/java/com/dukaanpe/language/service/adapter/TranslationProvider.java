package com.dukaanpe.language.service.adapter;

import java.util.Optional;

public interface TranslationProvider {

    Optional<String> translate(String normalizedText, String targetLanguage);
}

