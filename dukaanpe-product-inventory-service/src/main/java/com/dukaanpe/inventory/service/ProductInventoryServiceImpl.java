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
import com.dukaanpe.inventory.entity.Inventory;
import com.dukaanpe.inventory.entity.InventoryTransaction;
import com.dukaanpe.inventory.entity.InventoryTransactionType;
import com.dukaanpe.inventory.entity.Product;
import com.dukaanpe.inventory.entity.ProductCategory;
import com.dukaanpe.inventory.entity.ProductExpiry;
import com.dukaanpe.inventory.exception.ResourceNotFoundException;
import com.dukaanpe.inventory.repository.InventoryRepository;
import com.dukaanpe.inventory.repository.InventoryTransactionRepository;
import com.dukaanpe.inventory.repository.ProductCategoryRepository;
import com.dukaanpe.inventory.repository.ProductExpiryRepository;
import com.dukaanpe.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductInventoryServiceImpl implements ProductInventoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductExpiryRepository expiryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        ProductCategory category = ProductCategory.builder()
            .storeId(request.getStoreId())
            .categoryName(request.getCategoryName())
            .categoryNameHindi(request.getCategoryNameHindi())
            .parentCategoryId(request.getParentCategoryId())
            .displayOrder(request.getDisplayOrder())
            .isActive(true)
            .build();
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(Long storeId) {
        return categoryRepository.findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId)
            .stream().map(this::toCategoryResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        ProductCategory category = getActiveCategory(id);
        category.setCategoryName(request.getCategoryName());
        category.setCategoryNameHindi(request.getCategoryNameHindi());
        category.setParentCategoryId(request.getParentCategoryId());
        category.setDisplayOrder(request.getDisplayOrder());
        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = getActiveCategory(id);
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ProductResponse addProduct(ProductRequest request) {
        Product product = mapProduct(new Product(), request);
        Product saved = productRepository.save(product);
        ensureInventory(saved, request.getStoreId());
        return toProductResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> listProducts(Long storeId, int page, int size) {
        Page<Product> productPage = productRepository.findByStoreIdAndIsActiveTrue(
            storeId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "productName"))
        );
        return PagedResponse.<ProductResponse>builder()
            .content(productPage.getContent().stream().map(this::toProductResponse).toList())
            .pageNumber(productPage.getNumber())
            .pageSize(productPage.getSize())
            .totalElements(productPage.getTotalElements())
            .totalPages(productPage.getTotalPages())
            .last(productPage.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return toProductResponse(getActiveProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getActiveProduct(id);
        mapProduct(product, request);
        return toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = getActiveProduct(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(Long storeId, String query, Long categoryId, int page, int size) {
        String normalizedQuery = query.trim();
        Page<Product> productPage = productRepository.searchProducts(
            storeId,
            normalizedQuery,
            categoryId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "productName"))
        );
        return PagedResponse.<ProductResponse>builder()
            .content(productPage.getContent().stream().map(this::toProductResponse).toList())
            .pageNumber(productPage.getNumber())
            .pageSize(productPage.getSize())
            .totalElements(productPage.getTotalElements())
            .totalPages(productPage.getTotalPages())
            .last(productPage.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getByBarcode(Long storeId, String barcode) {
        Product product = productRepository.findByStoreIdAndBarcodeAndIsActiveTrue(storeId, barcode)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
        return toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByCategory(Long categoryId) {
        ProductCategory category = getActiveCategory(categoryId);
        return productRepository.findByStoreIdAndIsActiveTrueAndCategoryId(category.getStoreId(), categoryId)
            .stream().map(this::toProductResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory(Long storeId) {
        return inventoryRepository.findByStoreId(storeId).stream().map(this::toInventoryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryForProduct(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
        return toInventoryResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
        double previous = inventory.getCurrentStock();
        inventory.setCurrentStock(request.getCurrentStock());
        inventory.setMinStockLevel(request.getMinStockLevel());
        inventory.setMaxStockLevel(request.getMaxStockLevel());
        inventory.setReorderLevel(request.getReorderLevel());
        if (request.getCurrentStock() > previous) {
            inventory.setLastRestockedAt(LocalDateTime.now());
        }
        Inventory saved = inventoryRepository.save(inventory);
        recordTransaction(saved.getProduct(), saved.getStoreId(), InventoryTransactionType.ADJUSTMENT,
            Math.abs(saved.getCurrentStock() - previous), previous, saved.getCurrentStock(), "MANUAL", "Inventory update", "system");
        return toInventoryResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        Product product = getActiveProduct(request.getProductId());
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));

        double previous = inventory.getCurrentStock();
        double newStock = previous;

        if (request.getTransactionType() == InventoryTransactionType.SALE
            || request.getTransactionType() == InventoryTransactionType.WASTAGE) {
            newStock = Math.max(0, previous - request.getQuantity());
        } else {
            newStock = previous + request.getQuantity();
            inventory.setLastRestockedAt(LocalDateTime.now());
        }

        inventory.setCurrentStock(newStock);
        Inventory saved = inventoryRepository.save(inventory);

        recordTransaction(product, request.getStoreId(), request.getTransactionType(), request.getQuantity(),
            previous, newStock, request.getReferenceId(), request.getNotes(), request.getCreatedBy());

        return toInventoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> lowStock(Long storeId) {
        return inventoryRepository.findByStoreId(storeId).stream()
            .filter(item -> item.getMinStockLevel() != null && item.getCurrentStock() <= item.getMinStockLevel())
            .map(this::toInventoryResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> transactions(Long storeId, Long productId) {
        return transactionRepository.findByStoreIdAndProductIdOrderByCreatedAtDesc(storeId, productId).stream()
            .map(this::toTransactionResponse)
            .toList();
    }

    @Override
    @Transactional
    public ExpiryRecordResponse addExpiryRecord(ExpiryRecordRequest request) {
        Product product = getActiveProduct(request.getProductId());
        ProductExpiry expiry = ProductExpiry.builder()
            .product(product)
            .storeId(request.getStoreId())
            .batchNumber(request.getBatchNumber())
            .manufacturingDate(request.getManufacturingDate())
            .expiryDate(request.getExpiryDate())
            .quantity(request.getQuantity())
            .isExpired(request.getExpiryDate().isBefore(LocalDate.now()))
            .alertSent(false)
            .build();
        return toExpiryResponse(expiryRepository.save(expiry));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiryRecordResponse> upcomingExpiry(Long storeId, int days) {
        LocalDate now = LocalDate.now();
        LocalDate until = now.plusDays(days);
        return expiryRepository.findByStoreIdAndExpiryDateBetweenOrderByExpiryDateAsc(storeId, now, until)
            .stream().map(this::toExpiryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiryRecordResponse> expired(Long storeId) {
        return expiryRepository.findByStoreIdAndExpiryDateBeforeOrderByExpiryDateAsc(storeId, LocalDate.now())
            .stream().map(this::toExpiryResponse).toList();
    }

    private ProductCategory getActiveCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new ResourceNotFoundException("Category is inactive with id: " + id);
        }
        return category;
    }

    private Product getActiveProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ResourceNotFoundException("Product is inactive with id: " + id);
        }
        return product;
    }

    private Product mapProduct(Product product, ProductRequest request) {
        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = getActiveCategory(request.getCategoryId());
        }

        product.setStoreId(request.getStoreId());
        product.setCategory(category);
        product.setProductName(request.getProductName());
        product.setProductNameHindi(request.getProductNameHindi());
        product.setProductNameRegional(request.getProductNameRegional());
        product.setBarcode(request.getBarcode());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setHsnCode(request.getHsnCode());
        product.setBrand(request.getBrand());
        product.setMrp(request.getMrp());
        product.setSellingPrice(request.getSellingPrice());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setUnit(request.getUnit());
        product.setUnitQuantity(request.getUnitQuantity() != null ? request.getUnitQuantity() : 1.0);
        product.setGstRate(request.getGstRate());
        product.setImageUrl(request.getImageUrl());
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        return product;
    }

    private void ensureInventory(Product product, Long storeId) {
        inventoryRepository.findByProductId(product.getId()).orElseGet(() -> {
            Inventory inventory = Inventory.builder()
                .product(product)
                .storeId(storeId)
                .currentStock(0.0)
                .minStockLevel(0.0)
                .maxStockLevel(0.0)
                .reorderLevel(0.0)
                .build();
            return inventoryRepository.save(inventory);
        });
    }

    private void recordTransaction(
        Product product,
        Long storeId,
        InventoryTransactionType type,
        Double quantity,
        Double previous,
        Double current,
        String referenceId,
        String notes,
        String createdBy
    ) {
        InventoryTransaction transaction = InventoryTransaction.builder()
            .product(product)
            .storeId(storeId)
            .transactionType(type)
            .quantity(quantity)
            .previousStock(previous)
            .newStock(current)
            .referenceId(referenceId)
            .notes(notes)
            .createdBy(createdBy)
            .build();
        transactionRepository.save(transaction);
    }

    private CategoryResponse toCategoryResponse(ProductCategory category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .storeId(category.getStoreId())
            .categoryName(category.getCategoryName())
            .categoryNameHindi(category.getCategoryNameHindi())
            .parentCategoryId(category.getParentCategoryId())
            .displayOrder(category.getDisplayOrder())
            .isActive(category.getIsActive())
            .build();
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .storeId(product.getStoreId())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
            .productName(product.getProductName())
            .productNameHindi(product.getProductNameHindi())
            .productNameRegional(product.getProductNameRegional())
            .barcode(product.getBarcode())
            .sku(product.getSku())
            .description(product.getDescription())
            .hsnCode(product.getHsnCode())
            .brand(product.getBrand())
            .mrp(product.getMrp())
            .sellingPrice(product.getSellingPrice())
            .purchasePrice(product.getPurchasePrice())
            .unit(product.getUnit())
            .unitQuantity(product.getUnitQuantity())
            .gstRate(product.getGstRate())
            .imageUrl(product.getImageUrl())
            .isActive(product.getIsActive())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }

    private InventoryResponse toInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
            .id(inventory.getId())
            .productId(inventory.getProduct().getId())
            .productName(inventory.getProduct().getProductName())
            .storeId(inventory.getStoreId())
            .currentStock(inventory.getCurrentStock())
            .minStockLevel(inventory.getMinStockLevel())
            .maxStockLevel(inventory.getMaxStockLevel())
            .reorderLevel(inventory.getReorderLevel())
            .updatedAt(inventory.getUpdatedAt().toString())
            .build();
    }

    private InventoryTransactionResponse toTransactionResponse(InventoryTransaction transaction) {
        return InventoryTransactionResponse.builder()
            .id(transaction.getId())
            .productId(transaction.getProduct().getId())
            .productName(transaction.getProduct().getProductName())
            .storeId(transaction.getStoreId())
            .transactionType(transaction.getTransactionType())
            .quantity(transaction.getQuantity())
            .previousStock(transaction.getPreviousStock())
            .newStock(transaction.getNewStock())
            .referenceId(transaction.getReferenceId())
            .notes(transaction.getNotes())
            .createdBy(transaction.getCreatedBy())
            .createdAt(transaction.getCreatedAt().toString())
            .build();
    }

    private ExpiryRecordResponse toExpiryResponse(ProductExpiry expiry) {
        return ExpiryRecordResponse.builder()
            .id(expiry.getId())
            .productId(expiry.getProduct().getId())
            .productName(expiry.getProduct().getProductName())
            .storeId(expiry.getStoreId())
            .batchNumber(expiry.getBatchNumber())
            .manufacturingDate(expiry.getManufacturingDate())
            .expiryDate(expiry.getExpiryDate())
            .quantity(expiry.getQuantity())
            .isExpired(expiry.getIsExpired())
            .alertSent(expiry.getAlertSent())
            .build();
    }
}

