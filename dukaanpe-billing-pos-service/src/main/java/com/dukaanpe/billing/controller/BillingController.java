package com.dukaanpe.billing.controller;

import com.dukaanpe.billing.dto.ApiResponse;
import com.dukaanpe.billing.dto.BillResponse;
import com.dukaanpe.billing.dto.BillingSummaryResponse;
import com.dukaanpe.billing.dto.CreateBillRequest;
import com.dukaanpe.billing.dto.PagedResponse;
import com.dukaanpe.billing.service.BillingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Validated
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> create(@Valid @RequestBody CreateBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Bill created", billingService.createBill(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BillResponse>>> list(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(billingService.listBills(storeId, date, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(billingService.getBill(id)));
    }

    @GetMapping("/number/{billNumber}")
    public ResponseEntity<ApiResponse<BillResponse>> getByNumber(@PathVariable("billNumber") String billNumber) {
        return ResponseEntity.ok(ApiResponse.success(billingService.getBillByNumber(billNumber)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BillResponse>> cancel(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill cancelled", billingService.cancelBill(id)));
    }

    @GetMapping("/today-summary")
    public ResponseEntity<ApiResponse<BillingSummaryResponse>> todaySummary(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(billingService.todaySummary(storeId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BillResponse>>> search(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("q") String query,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(billingService.searchBills(storeId, query, page, size)));
    }
}

