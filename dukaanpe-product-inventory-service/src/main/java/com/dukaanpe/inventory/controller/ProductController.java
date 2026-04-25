package com.dukaanpe.inventory.controller;

import com.dukaanpe.inventory.dto.ApiResponse;
import com.dukaanpe.inventory.dto.CategoryRequest;
import com.dukaanpe.inventory.dto.CategoryResponse;
import com.dukaanpe.inventory.dto.PagedResponse;
import com.dukaanpe.inventory.dto.ProductRequest;
import com.dukaanpe.inventory.dto.ProductResponse;
import com.dukaanpe.inventory.service.ProductInventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductInventoryService service;

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Category created", service.createCategory(request)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.listCategories(storeId)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
        @PathVariable("id") Long id,
        @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", service.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Product created", service.addProduct(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> listProducts(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.listProducts(storeId, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getProduct(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
        @PathVariable("id") Long id,
        @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", service.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(@PathVariable("id") Long id) {
        service.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> search(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("q") @NotBlank(message = "q is required") @Size(min = 2, max = 100, message = "q length must be between 2 and 100") String query,
        @RequestParam(value = "categoryId", required = false) @Positive(message = "categoryId must be positive") Long categoryId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.searchProducts(storeId, query, categoryId, page, size)));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ApiResponse<ProductResponse>> byBarcode(
        @PathVariable("barcode") String barcode,
        @RequestParam("storeId") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getByBarcode(storeId, barcode)));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> byCategory(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByCategory(categoryId)));
    }
}

