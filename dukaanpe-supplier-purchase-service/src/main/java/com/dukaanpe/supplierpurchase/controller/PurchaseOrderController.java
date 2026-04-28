package com.dukaanpe.supplierpurchase.controller;

import com.dukaanpe.supplierpurchase.dto.ApiResponse;
import com.dukaanpe.supplierpurchase.dto.AutoReorderSuggestionResponse;
import com.dukaanpe.supplierpurchase.dto.PagedResponse;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderRequest;
import com.dukaanpe.supplierpurchase.dto.PurchaseOrderResponse;
import com.dukaanpe.supplierpurchase.dto.UpdatePurchaseOrderStatusRequest;
import com.dukaanpe.supplierpurchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Validated
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPurchaseOrder(
        @Valid @RequestBody PurchaseOrderRequest request
    ) {
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Purchase order created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PurchaseOrderResponse>>> listPurchaseOrders(
        @RequestParam("storeId") Long storeId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.listPurchaseOrders(storeId, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.getPurchaseOrder(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updatePurchaseOrder(
        @PathVariable Long id,
        @Valid @RequestBody PurchaseOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated", purchaseOrderService.updatePurchaseOrder(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePurchaseOrderStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order status updated", purchaseOrderService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.cancelPurchaseOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auto-suggest")
    public ResponseEntity<ApiResponse<List<AutoReorderSuggestionResponse>>> autoSuggest(
        @RequestParam("storeId") Long storeId,
        @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(purchaseOrderService.autoSuggest(storeId, limit)));
    }
}

