package com.dukaanpe.supplierpurchase.controller;

import com.dukaanpe.supplierpurchase.dto.ApiResponse;
import com.dukaanpe.supplierpurchase.dto.SupplierRequest;
import com.dukaanpe.supplierpurchase.dto.SupplierResponse;
import com.dukaanpe.supplierpurchase.service.SupplierService;
import jakarta.validation.Valid;
import java.util.List;
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

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Validated
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Supplier created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> listSuppliers(@RequestParam("storeId") Long storeId) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.listSuppliers(storeId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplier(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
        @PathVariable Long id,
        @Valid @RequestBody SupplierRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Supplier updated", supplierService.updateSupplier(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateSupplier(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> searchSuppliers(
        @RequestParam("storeId") Long storeId,
        @RequestParam("q") String q
    ) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.searchSuppliers(storeId, q)));
    }
}

