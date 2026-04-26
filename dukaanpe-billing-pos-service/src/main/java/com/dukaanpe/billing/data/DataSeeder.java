package com.dukaanpe.billing.data;

import com.dukaanpe.billing.dto.BillItemRequest;
import com.dukaanpe.billing.dto.CreateBillRequest;
import com.dukaanpe.billing.entity.PaymentMode;
import com.dukaanpe.billing.repository.BillRepository;
import com.dukaanpe.billing.service.BillingService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BillRepository billRepository;
    private final BillingService billingService;

    @Override
    public void run(String... args) {
        if (billRepository.count() > 0) {
            return;
        }

        createSampleBill(1L, "9876543201", "Ravi", PaymentMode.CASH,
            List.of(item(101L, "Amul Taza Milk", 2.0, 27), item(102L, "Parle-G Biscuit", 3.0, 10)));
        createSampleBill(1L, "9876543202", "Sita", PaymentMode.UPI,
            List.of(item(103L, "Fortune Sunflower Oil", 1.0, 145), item(104L, "Maggi Noodles", 5.0, 14)));
        createSampleBill(1L, null, null, PaymentMode.CARD,
            List.of(item(105L, "Surf Excel", 1.0, 235)));
        createSampleBill(1L, "9876543203", "Mohan", PaymentMode.CREDIT,
            List.of(item(106L, "Aashirvaad Atta", 1.0, 280), item(107L, "Tata Salt", 1.0, 28)));
        createSampleBill(1L, "9876543204", "Aarti", PaymentMode.MIXED,
            List.of(item(108L, "Colgate Toothpaste", 2.0, 95), item(109L, "Haldiram Bhujia", 1.0, 55)));
    }

    private void createSampleBill(Long storeId, String phone, String name, PaymentMode mode, List<BillItemRequest> items) {
        CreateBillRequest req = new CreateBillRequest();
        req.setStoreId(storeId);
        req.setCustomerPhone(phone);
        req.setCustomerName(name);
        req.setPaymentMode(mode);
        req.setItems(items);
        req.setCreatedBy("seed");

        if (mode == PaymentMode.CASH) {
            req.setCashAmount(new BigDecimal("500"));
        } else if (mode == PaymentMode.UPI) {
            req.setUpiAmount(new BigDecimal("500"));
        } else if (mode == PaymentMode.CARD) {
            req.setCardAmount(new BigDecimal("500"));
        } else if (mode == PaymentMode.CREDIT) {
            req.setCreditAmount(new BigDecimal("500"));
        } else {
            req.setCashAmount(new BigDecimal("200"));
            req.setUpiAmount(new BigDecimal("300"));
        }

        billingService.createBill(req);
    }

    private BillItemRequest item(Long id, String name, Double qty, int price) {
        BillItemRequest item = new BillItemRequest();
        item.setProductId(id);
        item.setProductName(name);
        item.setQuantity(qty);
        item.setUnit("PIECE");
        item.setMrp(BigDecimal.valueOf(price));
        item.setUnitPrice(BigDecimal.valueOf(price));
        item.setDiscountPercent(BigDecimal.ZERO);
        item.setGstRate(new BigDecimal("5"));
        return item;
    }
}

