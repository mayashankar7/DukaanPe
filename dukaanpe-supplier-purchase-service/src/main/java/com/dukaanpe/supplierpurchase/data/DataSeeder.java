package com.dukaanpe.supplierpurchase.data;

import com.dukaanpe.supplierpurchase.entity.PurchaseOrder;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderItem;
import com.dukaanpe.supplierpurchase.entity.PurchaseOrderStatus;
import com.dukaanpe.supplierpurchase.entity.Supplier;
import com.dukaanpe.supplierpurchase.repository.PurchaseOrderRepository;
import com.dukaanpe.supplierpurchase.repository.SupplierRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public void run(String... args) {
        if (supplierRepository.count() > 0 || purchaseOrderRepository.count() > 0) {
            return;
        }

        Supplier supplier1 = supplierRepository.save(Supplier.builder()
            .storeId(1L)
            .supplierName("Pune Grocery Wholesalers")
            .contactPerson("Nitin Patil")
            .phone("9876500011")
            .city("Pune")
            .state("Maharashtra")
            .pincode("411001")
            .categoriesSupplied("Dairy,Snacks,Beverages")
            .paymentTerms("Net 15")
            .rating(5)
            .build());

        Supplier supplier2 = supplierRepository.save(Supplier.builder()
            .storeId(1L)
            .supplierName("Sharma Med Supplies")
            .contactPerson("Suresh Sharma")
            .phone("9876500012")
            .city("Delhi")
            .state("Delhi")
            .pincode("110001")
            .categoriesSupplied("Pharma,Personal Care")
            .paymentTerms("COD")
            .rating(4)
            .build());

        supplierRepository.save(Supplier.builder()
            .storeId(2L)
            .supplierName("Capital Retail Distributors")
            .contactPerson("Aman Mehta")
            .phone("9876500013")
            .city("Delhi")
            .state("Delhi")
            .pincode("110005")
            .categoriesSupplied("General,Household")
            .paymentTerms("Net 30")
            .rating(4)
            .build());

        seedPo(
            1L,
            supplier1.getId(),
            "PO-20260325-001",
            LocalDate.now(),
            LocalDate.now().plusDays(2),
            PurchaseOrderStatus.SENT,
            List.of(
                item("Amul Taza Milk", 50.0, "PIECE", new BigDecimal("25.00"), new BigDecimal("0.00")),
                item("Parle-G Biscuit", 200.0, "PACKET", new BigDecimal("8.00"), new BigDecimal("5.00"))
            )
        );

        seedPo(
            1L,
            supplier2.getId(),
            "PO-20260325-002",
            LocalDate.now(),
            LocalDate.now().plusDays(3),
            PurchaseOrderStatus.DRAFT,
            List.of(
                item("Hand Sanitizer 100ml", 120.0, "BOTTLE", new BigDecimal("35.00"), new BigDecimal("18.00")),
                item("Cotton Roll", 60.0, "PIECE", new BigDecimal("18.00"), new BigDecimal("12.00"))
            )
        );
    }

    private PurchaseOrderItem item(String name, Double qty, String unit, BigDecimal unitPrice, BigDecimal gstRate) {
        return PurchaseOrderItem.builder()
            .productName(name)
            .quantityOrdered(qty)
            .quantityReceived(0.0)
            .unit(unit)
            .unitPrice(unitPrice)
            .gstRate(gstRate)
            .totalAmount(calculateLineTotal(unitPrice, qty, gstRate))
            .build();
    }

    private void seedPo(
        Long storeId,
        Long supplierId,
        String poNumber,
        LocalDate orderDate,
        LocalDate expectedDate,
        PurchaseOrderStatus status,
        List<PurchaseOrderItem> items
    ) {
        PurchaseOrder order = new PurchaseOrder();
        order.setStoreId(storeId);
        order.setSupplierId(supplierId);
        order.setPoNumber(poNumber);
        order.setOrderDate(orderDate);
        order.setExpectedDeliveryDate(expectedDate);
        order.setStatus(status);
        order.setCreatedBy("seeder");

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItem item : items) {
            item.setPurchaseOrder(order);
            subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantityOrdered())));
            total = total.add(item.getTotalAmount());
        }

        order.setItems(items);
        order.setSubtotal(subtotal.setScale(2, java.math.RoundingMode.HALF_UP));
        order.setTotalAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
        order.setGstAmount(order.getTotalAmount().subtract(order.getSubtotal()));
        purchaseOrderRepository.save(order);
    }

    private BigDecimal calculateLineTotal(BigDecimal unitPrice, Double quantity, BigDecimal gstRate) {
        BigDecimal base = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal gst = base.multiply(gstRate).divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        return base.add(gst).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}

