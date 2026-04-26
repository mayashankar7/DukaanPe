package com.dukaanpe.billing.controller;

import com.dukaanpe.billing.dto.ApiResponse;
import com.dukaanpe.billing.dto.CalculateBillRequest;
import com.dukaanpe.billing.dto.CalculateBillResponse;
import com.dukaanpe.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class PosController {

    private final BillingService billingService;

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CalculateBillResponse>> calculate(@Valid @RequestBody CalculateBillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(billingService.calculate(request)));
    }
}

