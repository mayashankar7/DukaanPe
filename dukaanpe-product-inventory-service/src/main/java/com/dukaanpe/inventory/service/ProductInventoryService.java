package com.dukaanpe.inventory.service;

import com.dukaanpe.inventory.dto.CategoryRequest;
import com.dukaanpe.inventory.dto.CategoryResponse;
import com.dukaanpe.inventory.dto.ExpiryRecordRequest;
import com.dukaanpe.inventory.dto.ExpiryRecordResponse;
import com.dukaanpe.inventory.dto.InventoryResponse;
import com.dukaanpe.inventory.dto.InventoryTransactionResponse;
import com.dukaanpe.inventory.dto.InventoryUpdateRequest;
import com.dukaanpe.inventory.dto.PagedResponse;
import com.dukaanpe.inventory.dto.ProductRequest;
import com.dukaanpe.inventory.dto.ProductResponse;
import com.dukaanpe.inventory.dto.StockAdjustmentRequest;
import java.util.List;

public interface ProductInventoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> listCategories(Long storeId);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    ProductResponse addProduct(ProductRequest request);

    PagedResponse<ProductResponse> listProducts(Long storeId, int page, int size);

    ProductResponse getProduct(Long id);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deactivateProduct(Long id);

    PagedResponse<ProductResponse> searchProducts(Long storeId, String query, Long categoryId, int page, int size);

    ProductResponse getByBarcode(Long storeId, String barcode);

    List<ProductResponse> getByCategory(Long categoryId);

    List<InventoryResponse> getInventory(Long storeId);

    InventoryResponse getInventoryForProduct(Long productId);

    InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request);

    InventoryResponse adjustStock(StockAdjustmentRequest request);

    List<InventoryResponse> lowStock(Long storeId);

    List<InventoryTransactionResponse> transactions(Long storeId, Long productId);

    ExpiryRecordResponse addExpiryRecord(ExpiryRecordRequest request);

    List<ExpiryRecordResponse> upcomingExpiry(Long storeId, int days);

    List<ExpiryRecordResponse> expired(Long storeId);
}

