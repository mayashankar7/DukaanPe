package com.dukaanpe.language.service;

import com.dukaanpe.language.dto.TranslateRequest;
import com.dukaanpe.language.dto.TranslateResponse;

import java.util.List;

public interface LanguageService {

    TranslateResponse translate(TranslateRequest request);

    List<String> supportedLanguages();
}

