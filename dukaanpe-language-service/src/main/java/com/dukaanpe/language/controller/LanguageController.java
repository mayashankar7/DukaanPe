package com.dukaanpe.language.controller;

import com.dukaanpe.language.dto.ApiResponse;
import com.dukaanpe.language.dto.TranslateRequest;
import com.dukaanpe.language.dto.TranslateResponse;
import com.dukaanpe.language.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/language")
@RequiredArgsConstructor
@Validated
public class LanguageController {

    private final LanguageService languageService;

    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<TranslateResponse>> translate(@Valid @RequestBody TranslateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(languageService.translate(request)));
    }

    @GetMapping("/supported")
    public ResponseEntity<ApiResponse<List<String>>> supported() {
        return ResponseEntity.ok(ApiResponse.success(languageService.supportedLanguages()));
    }
}

