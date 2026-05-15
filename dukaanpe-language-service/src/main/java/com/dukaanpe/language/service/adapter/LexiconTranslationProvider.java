package com.dukaanpe.language.service.adapter;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class LexiconTranslationProvider implements TranslationProvider {

    private static final Map<String, Map<String, String>> LEXICON = Map.of(
        "hello", Map.of("hi", "namaste", "ta", "vanakkam", "mr", "namaskar"),
        "thank you", Map.of("hi", "dhanyavaad", "ta", "nandri", "mr", "dhanyavad"),
        "total", Map.of("hi", "kul", "ta", "mottam", "mr", "ekun")
    );

    @Override
    public Optional<String> translate(String normalizedText, String targetLanguage) {
        return Optional.ofNullable(LEXICON.getOrDefault(normalizedText, Map.of()).get(targetLanguage));
    }
}

