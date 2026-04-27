package com.dukaanpe.udhar.data;

import com.dukaanpe.udhar.dto.CreateKhataCustomerRequest;
import com.dukaanpe.udhar.dto.CreditRequest;
import com.dukaanpe.udhar.dto.KhataCustomerResponse;
import com.dukaanpe.udhar.dto.PaymentRequest;
import com.dukaanpe.udhar.entity.UdharPaymentMode;
import com.dukaanpe.udhar.repository.KhataCustomerRepository;
import com.dukaanpe.udhar.service.UdharKhataService;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final KhataCustomerRepository khataCustomerRepository;
    private final UdharKhataService udharKhataService;

    @Override
    public void run(String... args) {
        if (khataCustomerRepository.count() > 0) {
            return;
        }

        KhataCustomerResponse c1 = createCustomer(1L, "Ramesh Kumar", "9876500010", new BigDecimal("20000"), "Regular family monthly khata");
        KhataCustomerResponse c2 = createCustomer(1L, "Sunita Devi", "9876500011", new BigDecimal("12000"), "Weekly settlement");
        KhataCustomerResponse c3 = createCustomer(1L, "Mohan Lal", "9876500012", new BigDecimal("10000"), "Seasonal buyer");
        KhataCustomerResponse c4 = createCustomer(1L, "Aarti Bai", "9876500013", new BigDecimal("8000"), "Pays quickly");
        KhataCustomerResponse c5 = createCustomer(1L, "Dinesh Ji", "9876500014", new BigDecimal("15000"), "Delayed payer");

        credit(c1.getId(), new BigDecimal("9000"), "Monthly groceries", LocalDate.now().minusDays(15));
        payment(c1.getId(), new BigDecimal("1500"), "UPI settlement", UdharPaymentMode.UPI, "UPI-RAMESH-1");

        credit(c2.getId(), new BigDecimal("6500"), "Festival purchase", LocalDate.now().minusDays(5));
        payment(c2.getId(), new BigDecimal("1000"), "Cash partial", UdharPaymentMode.CASH, "CASH-SUNITA-1");

        credit(c3.getId(), new BigDecimal("3200"), "Atta, oil and pulses", LocalDate.now().plusDays(7));
        credit(c4.getId(), new BigDecimal("2100"), "Daily essentials", LocalDate.now().plusDays(10));
        payment(c4.getId(), new BigDecimal("600"), "Immediate part payment", UdharPaymentMode.CASH, "CASH-AARTI-1");

        credit(c5.getId(), new BigDecimal("12300"), "Bulk stock", LocalDate.now().minusDays(35));
    }

    private KhataCustomerResponse createCustomer(Long storeId, String name, String phone, BigDecimal creditLimit, String notes) {
        CreateKhataCustomerRequest request = new CreateKhataCustomerRequest();
        request.setStoreId(storeId);
        request.setCustomerName(name);
        request.setCustomerPhone(phone);
        request.setCreditLimit(creditLimit);
        request.setNotes(notes);
        return udharKhataService.addCustomer(request);
    }

    private void credit(Long customerId, BigDecimal amount, String description, LocalDate dueDate) {
        CreditRequest request = new CreditRequest();
        request.setStoreId(1L);
        request.setKhataCustomerId(customerId);
        request.setAmount(amount);
        request.setDescription(description);
        request.setItemsSummary(description);
        request.setDueDate(dueDate);
        request.setCreatedBy("seed");
        udharKhataService.giveCredit(request);
    }

    private void payment(Long customerId, BigDecimal amount, String description, UdharPaymentMode mode, String ref) {
        PaymentRequest request = new PaymentRequest();
        request.setStoreId(1L);
        request.setKhataCustomerId(customerId);
        request.setAmount(amount);
        request.setDescription(description);
        request.setPaymentMode(mode);
        request.setReferenceNumber(ref);
        request.setCreatedBy("seed");
        udharKhataService.recordPayment(request);
    }
}

