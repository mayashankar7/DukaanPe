package com.dukaanpe.payment.data;

import com.dukaanpe.payment.entity.PaymentMode;
import com.dukaanpe.payment.entity.PaymentReconciliation;
import com.dukaanpe.payment.entity.PaymentStatus;
import com.dukaanpe.payment.entity.PaymentTransaction;
import com.dukaanpe.payment.entity.ReconciliationStatus;
import com.dukaanpe.payment.entity.UpiQrCode;
import com.dukaanpe.payment.repository.PaymentReconciliationRepository;
import com.dukaanpe.payment.repository.PaymentTransactionRepository;
import com.dukaanpe.payment.repository.UpiQrCodeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UpiQrCodeRepository upiQrCodeRepository;
    private final PaymentReconciliationRepository paymentReconciliationRepository;

    @Override
    public void run(String... args) {
        if (paymentTransactionRepository.count() > 0) {
            return;
        }

        PaymentTransaction completed = PaymentTransaction.builder()
            .storeId(1L)
            .transactionId("TXN-SEED-0001")
            .billId(101L)
            .amount(new BigDecimal("450.00"))
            .paymentMode(PaymentMode.UPI)
            .paymentStatus(PaymentStatus.COMPLETED)
            .upiId("rajesh@upi")
            .upiReference("UPI-REF-1001")
            .payerName("Rajesh Kumar")
            .payerPhone("9876543210")
            .description("Bill payment")
            .build();

        PaymentTransaction initiated = PaymentTransaction.builder()
            .storeId(1L)
            .transactionId("TXN-SEED-0002")
            .billId(102L)
            .amount(new BigDecimal("1200.00"))
            .paymentMode(PaymentMode.CARD)
            .paymentStatus(PaymentStatus.INITIATED)
            .cardLastFour("8842")
            .payerName("Priya Sharma")
            .payerPhone("9876543211")
            .description("Card pending completion")
            .build();

        PaymentTransaction failed = PaymentTransaction.builder()
            .storeId(2L)
            .transactionId("TXN-SEED-0003")
            .udharEntryId(501L)
            .amount(new BigDecimal("300.00"))
            .paymentMode(PaymentMode.NET_BANKING)
            .paymentStatus(PaymentStatus.FAILED)
            .payerName("Amit Singh")
            .payerPhone("9876543212")
            .description("Settlement attempt")
            .failureReason("Bank timeout")
            .build();

        paymentTransactionRepository.saveAll(List.of(completed, initiated, failed));

        UpiQrCode defaultQr = UpiQrCode.builder()
            .storeId(1L)
            .merchantUpiId("rajesh@upi")
            .merchantName("Rajesh General Store")
            .qrCodeImageBase64("seed-qr-base64")
            .isDefault(true)
            .build();

        PaymentReconciliation reconciliation = PaymentReconciliation.builder()
            .storeId(1L)
            .reconDate(LocalDate.now())
            .totalCash(new BigDecimal("850.00"))
            .totalUpi(new BigDecimal("1450.00"))
            .totalCard(new BigDecimal("400.00"))
            .totalCollections(new BigDecimal("2700.00"))
            .cashInHand(new BigDecimal("850.00"))
            .discrepancy(BigDecimal.ZERO)
            .status(ReconciliationStatus.MATCHED)
            .notes("Seeded daily reconciliation")
            .build();

        upiQrCodeRepository.save(defaultQr);
        paymentReconciliationRepository.save(reconciliation);
    }
}

