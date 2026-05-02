package com.dukaanpe.gsttax.controller;

import com.dukaanpe.gsttax.dto.ApiResponse;
import com.dukaanpe.gsttax.dto.TaxSummaryResponse;
import com.dukaanpe.gsttax.service.TaxSummaryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
@Validated
public class TaxSummaryController {

    private final TaxSummaryService taxSummaryService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TaxSummaryResponse>> getSummary(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(ApiResponse.success(taxSummaryService.summarize(storeId, fromDate, toDate)));
    }
}

