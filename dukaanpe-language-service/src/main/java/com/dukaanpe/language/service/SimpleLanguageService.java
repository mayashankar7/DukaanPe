package com.dukaanpe.language.service;

import com.dukaanpe.language.dto.TranslateRequest;
import com.dukaanpe.language.dto.TranslateResponse;
import com.dukaanpe.language.entity.TranslationAuditEntity;
import com.dukaanpe.language.repository.TranslationAuditRepository;
import com.dukaanpe.language.service.adapter.TranslationProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SimpleLanguageService implements LanguageService {

    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "hi", "ta", "te", "mr", "bn");
    private final List<TranslationProvider> translationProviders;
    private final TranslationAuditRepository translationAuditRepository;
    @Value("${language.glossary.default-version:v1}")
    private String glossaryVersion;

    @Override
    public TranslateResponse translate(TranslateRequest request) {
        String source = request.getSourceLanguage().trim().toLowerCase(Locale.ROOT);
        String target = request.getTargetLanguage().trim().toLowerCase(Locale.ROOT);
        String normalizedText = request.getText().trim().toLowerCase(Locale.ROOT);

        validateLanguage(source, "sourceLanguage");
        validateLanguage(target, "targetLanguage");

        String providerName = "fallback";
        String translated = request.getText().trim() + " [" + target + "]";
        for (TranslationProvider provider : translationProviders) {
            var maybeTranslation = provider.translate(normalizedText, target);
            if (maybeTranslation.isPresent()) {
                translated = maybeTranslation.get();
                providerName = provider.getClass().getSimpleName();
                break;
            }
        }

        translationAuditRepository.save(TranslationAuditEntity.builder()
            .sourceLanguage(source)
            .targetLanguage(target)
            .originalText(request.getText().trim())
            .translatedText(translated)
            .provider(providerName)
            .glossaryVersion(glossaryVersion)
            .createdAt(LocalDateTime.now())
            .build());

        return TranslateResponse.builder()
            .sourceLanguage(source)
            .targetLanguage(target)
            .originalText(request.getText().trim())
            .translatedText(translated)
            .build();
    }

    @Override
    public List<String> supportedLanguages() {
        return SUPPORTED_LANGUAGES.stream().sorted().toList();
    }

    private void validateLanguage(String language, String fieldName) {
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException(fieldName + " is not supported: " + language);
        }
    }
}

