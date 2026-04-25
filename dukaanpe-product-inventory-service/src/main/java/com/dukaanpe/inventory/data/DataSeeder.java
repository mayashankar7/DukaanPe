package com.dukaanpe.inventory.data;

import com.dukaanpe.inventory.entity.Inventory;
import com.dukaanpe.inventory.entity.Product;
import com.dukaanpe.inventory.entity.ProductCategory;
import com.dukaanpe.inventory.entity.ProductExpiry;
import com.dukaanpe.inventory.entity.ProductUnit;
import com.dukaanpe.inventory.repository.InventoryRepository;
import com.dukaanpe.inventory.repository.ProductCategoryRepository;
import com.dukaanpe.inventory.repository.ProductExpiryRepository;
import com.dukaanpe.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductExpiryRepository expiryRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        Long storeId = 1L;
        Map<String, ProductCategory> categories = new HashMap<>();
        categories.put("Dairy", saveCategory(storeId, "Dairy", "डेयरी", 1));
        categories.put("Snacks", saveCategory(storeId, "Snacks", "नाश्ता", 2));
        categories.put("Beverages", saveCategory(storeId, "Beverages", "पेय", 3));
        categories.put("Grains", saveCategory(storeId, "Grains", "अनाज", 4));
        categories.put("Cleaning", saveCategory(storeId, "Cleaning", "सफाई", 5));
        categories.put("Personal Care", saveCategory(storeId, "Personal Care", "पर्सनल केयर", 6));

        List<Product> products = List.of(
            addProduct(storeId, categories.get("Dairy"), "Amul Taza Milk", "0401", new BigDecimal("29"), new BigDecimal("27"), new BigDecimal("25"), ProductUnit.PIECE, 1.0, new BigDecimal("0"), "890126200001"),
            addProduct(storeId, categories.get("Snacks"), "Parle-G Biscuit", "1905", new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("8"), ProductUnit.PACKET, 1.0, new BigDecimal("5"), "890171900011"),
            addProduct(storeId, categories.get("Grains"), "Tata Salt", "2501", new BigDecimal("30"), new BigDecimal("28"), new BigDecimal("24"), ProductUnit.KG, 1.0, new BigDecimal("5"), "890171900012"),
            addProduct(storeId, categories.get("Beverages"), "Fortune Sunflower Oil", "1512", new BigDecimal("155"), new BigDecimal("145"), new BigDecimal("135"), ProductUnit.LITRE, 1.0, new BigDecimal("5"), "890171900013"),
            addProduct(storeId, categories.get("Snacks"), "Maggi Noodles", "1902", new BigDecimal("14"), new BigDecimal("14"), new BigDecimal("11"), ProductUnit.PACKET, 1.0, new BigDecimal("5"), "890171900014"),
            addProduct(storeId, categories.get("Grains"), "Aashirvaad Atta", "1101", new BigDecimal("300"), new BigDecimal("280"), new BigDecimal("250"), ProductUnit.KG, 5.0, new BigDecimal("5"), "890171900015"),
            addProduct(storeId, categories.get("Beverages"), "Coca-Cola", "2202", new BigDecimal("40"), new BigDecimal("40"), new BigDecimal("33"), ProductUnit.BOTTLE, 0.75, new BigDecimal("28"), "890171900016"),
            addProduct(storeId, categories.get("Cleaning"), "Surf Excel", "3402", new BigDecimal("250"), new BigDecimal("235"), new BigDecimal("210"), ProductUnit.KG, 1.0, new BigDecimal("18"), "890171900017"),
            addProduct(storeId, categories.get("Personal Care"), "Colgate Toothpaste", "3306", new BigDecimal("99"), new BigDecimal("95"), new BigDecimal("80"), ProductUnit.PIECE, 0.1, new BigDecimal("18"), "890171900018"),
            addProduct(storeId, categories.get("Snacks"), "Haldiram Bhujia", "2106", new BigDecimal("60"), new BigDecimal("55"), new BigDecimal("45"), ProductUnit.PACKET, 0.2, new BigDecimal("12"), "890171900019"),
            addProduct(storeId, categories.get("Dairy"), "Amul Butter", "0405", new BigDecimal("58"), new BigDecimal("55"), new BigDecimal("49"), ProductUnit.PACKET, 0.1, new BigDecimal("12"), "890171900020"),
            addProduct(storeId, categories.get("Beverages"), "Frooti", "2202", new BigDecimal("20"), new BigDecimal("20"), new BigDecimal("16"), ProductUnit.PACKET, 0.2, new BigDecimal("12"), "890171900021"),
            addProduct(storeId, categories.get("Grains"), "Basmati Rice", "1006", new BigDecimal("160"), new BigDecimal("150"), new BigDecimal("130"), ProductUnit.KG, 1.0, new BigDecimal("5"), "890171900022"),
            addProduct(storeId, categories.get("Grains"), "Chana Dal", "0713", new BigDecimal("120"), new BigDecimal("110"), new BigDecimal("95"), ProductUnit.KG, 1.0, new BigDecimal("5"), "890171900023"),
            addProduct(storeId, categories.get("Cleaning"), "Harpic Toilet Cleaner", "3808", new BigDecimal("115"), new BigDecimal("105"), new BigDecimal("90"), ProductUnit.BOTTLE, 0.5, new BigDecimal("18"), "890171900024"),
            addProduct(storeId, categories.get("Personal Care"), "Dove Soap", "3401", new BigDecimal("45"), new BigDecimal("42"), new BigDecimal("35"), ProductUnit.PIECE, 1.0, new BigDecimal("18"), "890171900025"),
            addProduct(storeId, categories.get("Dairy"), "Mother Dairy Curd", "0403", new BigDecimal("35"), new BigDecimal("32"), new BigDecimal("27"), ProductUnit.PACKET, 0.4, new BigDecimal("5"), "890171900026"),
            addProduct(storeId, categories.get("Snacks"), "Kurkure Masala", "1905", new BigDecimal("20"), new BigDecimal("20"), new BigDecimal("16"), ProductUnit.PACKET, 1.0, new BigDecimal("12"), "890171900027"),
            addProduct(storeId, categories.get("Beverages"), "Real Mango Juice", "2009", new BigDecimal("130"), new BigDecimal("120"), new BigDecimal("100"), ProductUnit.LITRE, 1.0, new BigDecimal("12"), "890171900028"),
            addProduct(storeId, categories.get("Personal Care"), "Clinic Plus Shampoo", "3305", new BigDecimal("3"), new BigDecimal("2"), new BigDecimal("1.5"), ProductUnit.PACKET, 0.008, new BigDecimal("18"), "890171900029")
        );

        products.forEach(this::seedInventoryAndExpiry);
    }

    private ProductCategory saveCategory(Long storeId, String name, String hindi, int order) {
        return categoryRepository.save(ProductCategory.builder()
            .storeId(storeId)
            .categoryName(name)
            .categoryNameHindi(hindi)
            .displayOrder(order)
            .isActive(true)
            .build());
    }

    private Product addProduct(Long storeId, ProductCategory category, String name, String hsn,
                               BigDecimal mrp, BigDecimal selling, BigDecimal purchase,
                               ProductUnit unit, Double quantity, BigDecimal gst, String barcode) {
        return productRepository.save(Product.builder()
            .storeId(storeId)
            .category(category)
            .productName(name)
            .hsnCode(hsn)
            .mrp(mrp)
            .sellingPrice(selling)
            .purchasePrice(purchase)
            .unit(unit)
            .unitQuantity(quantity)
            .gstRate(gst)
            .barcode(barcode)
            .sku("SKU-" + barcode.substring(barcode.length() - 4))
            .isActive(true)
            .build());
    }

    private void seedInventoryAndExpiry(Product product) {
        inventoryRepository.save(Inventory.builder()
            .product(product)
            .storeId(product.getStoreId())
            .currentStock(40.0)
            .minStockLevel(8.0)
            .maxStockLevel(120.0)
            .reorderLevel(15.0)
            .lastRestockedAt(LocalDate.now().atStartOfDay())
            .build());

        if (product.getProductName().contains("Milk") || product.getProductName().contains("Curd") || product.getProductName().contains("Butter")) {
            expiryRepository.save(ProductExpiry.builder()
                .product(product)
                .storeId(product.getStoreId())
                .batchNumber("BATCH-" + product.getId())
                .manufacturingDate(LocalDate.now().minusDays(2))
                .expiryDate(LocalDate.now().plusDays(5))
                .quantity(15.0)
                .isExpired(false)
                .alertSent(false)
                .build());
        }
    }
}

