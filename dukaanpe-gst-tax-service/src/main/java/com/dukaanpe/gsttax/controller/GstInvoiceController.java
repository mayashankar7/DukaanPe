package com.dukaanpe.gsttax.controller;

import com.dukaanpe.gsttax.dto.ApiResponse;
import com.dukaanpe.gsttax.dto.GenerateGstInvoiceRequest;
import com.dukaanpe.gsttax.dto.GstInvoiceResponse;
import com.dukaanpe.gsttax.dto.PagedResponse;
import com.dukaanpe.gsttax.service.GstInvoiceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/gst/invoices")
@RequiredArgsConstructor
@Validated
public class GstInvoiceController {

    private final GstInvoiceService gstInvoiceService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GstInvoiceResponse>> generate(@Valid @RequestBody GenerateGstInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("GST invoice generated", gstInvoiceService.generate(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GstInvoiceResponse>> getById(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(gstInvoiceService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<GstInvoiceResponse>>> list(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(gstInvoiceService.listByDateRange(storeId, fromDate, toDate, page, size)));
    }
}

