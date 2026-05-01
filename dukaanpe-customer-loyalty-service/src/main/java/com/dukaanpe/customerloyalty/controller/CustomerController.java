package com.dukaanpe.customerloyalty.controller;

import com.dukaanpe.customerloyalty.dto.ApiResponse;
import com.dukaanpe.customerloyalty.dto.CreateCustomerRequest;
import com.dukaanpe.customerloyalty.dto.CustomerResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.PurchaseHistoryResponse;
import com.dukaanpe.customerloyalty.dto.RecordPurchaseRequest;
import com.dukaanpe.customerloyalty.dto.UpdateCustomerRequest;
import com.dukaanpe.customerloyalty.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Customer created", customerService.createCustomer(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> listCustomers(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(customerService.listCustomers(storeId, page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> searchCustomers(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "tier", required = false) String tier,
        @RequestParam(value = "minPurchases", required = false) @DecimalMin(value = "0.0", message = "minPurchases must be >= 0")
        BigDecimal minPurchases,
        @RequestParam(value = "lastVisitFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastVisitFrom,
        @RequestParam(value = "lastVisitTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate lastVisitTo,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(customerService.searchCustomers(
            storeId,
            query,
            tier,
            minPurchases,
            lastVisitFrom,
            lastVisitTo,
            page,
            size
        )));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<CustomerResponse>> findByPhone(
        @PathVariable("phone") @Pattern(regexp = "^[6-9][0-9]{9}$", message = "phone must be a valid 10 digit Indian mobile") String phone,
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findByPhone(storeId, phone)));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> topCustomers(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "limit", defaultValue = "10") @Min(value = 1, message = "limit must be >= 1")
        @Max(value = 50, message = "limit must be <= 50") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(customerService.topCustomers(storeId, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomer(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
        @PathVariable("id") @Positive(message = "id must be positive") Long id,
        @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Customer updated", customerService.updateCustomer(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCustomer(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        customerService.deactivateCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/purchases")
    public ResponseEntity<ApiResponse<PurchaseHistoryResponse>> recordPurchase(
        @PathVariable("id") @Positive(message = "id must be positive") Long customerId,
        @Valid @RequestBody RecordPurchaseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Purchase recorded", customerService.recordPurchase(customerId, request)));
    }

    @GetMapping("/{id}/purchases")
    public ResponseEntity<ApiResponse<PagedResponse<PurchaseHistoryResponse>>> getPurchases(
        @PathVariable("id") @Positive(message = "id must be positive") Long customerId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getPurchaseHistory(customerId, page, size)));
    }
}

