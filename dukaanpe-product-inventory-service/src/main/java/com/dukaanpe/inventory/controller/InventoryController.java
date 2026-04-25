package com.dukaanpe.inventory.controller;

import com.dukaanpe.inventory.dto.ApiResponse;
import com.dukaanpe.inventory.dto.ExpiryRecordRequest;
import com.dukaanpe.inventory.dto.ExpiryRecordResponse;
import com.dukaanpe.inventory.dto.InventoryResponse;
import com.dukaanpe.inventory.dto.InventoryTransactionResponse;
import com.dukaanpe.inventory.dto.InventoryUpdateRequest;
import com.dukaanpe.inventory.dto.StockAdjustmentRequest;
import com.dukaanpe.inventory.service.ProductInventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final ProductInventoryService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventory(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getInventory(storeId)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryForProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(ApiResponse.success(service.getInventoryForProduct(productId)));
    }

    @PutMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
        @PathVariable("productId") Long productId,
        @Valid @RequestBody InventoryUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Inventory updated", service.updateInventory(productId, request)));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", service.adjustStock(request)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> lowStock(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.lowStock(storeId)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<InventoryTransactionResponse>>> transactions(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("productId") @Positive(message = "productId must be positive") Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.transactions(storeId, productId)));
    }

    @PostMapping("/expiry")
    public ResponseEntity<ApiResponse<ExpiryRecordResponse>> addExpiry(@Valid @RequestBody ExpiryRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Expiry record added", service.addExpiryRecord(request)));
    }

    @GetMapping("/expiry/upcoming")
    public ResponseEntity<ApiResponse<List<ExpiryRecordResponse>>> upcoming(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "days", defaultValue = "30") @Min(value = 1, message = "days must be >= 1") int days
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.upcomingExpiry(storeId, days)));
    }

    @GetMapping("/expiry/expired")
    public ResponseEntity<ApiResponse<List<ExpiryRecordResponse>>> expired(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.expired(storeId)));
    }
}

