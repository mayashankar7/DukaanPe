package com.dukaanpe.payment.service;

import com.dukaanpe.payment.dto.CompletePaymentRequest;
import com.dukaanpe.payment.dto.FailPaymentRequest;
import com.dukaanpe.payment.dto.InitiatePaymentRequest;
import com.dukaanpe.payment.dto.PagedResponse;
import com.dukaanpe.payment.dto.PaymentTransactionResponse;
import com.dukaanpe.payment.dto.RefundPaymentRequest;
import com.dukaanpe.payment.entity.PaymentStatus;
import com.dukaanpe.payment.entity.PaymentTransaction;
import com.dukaanpe.payment.entity.IdempotencyRecord;
import com.dukaanpe.payment.exception.InvalidPaymentStateException;
import com.dukaanpe.payment.exception.ResourceNotFoundException;
import com.dukaanpe.payment.repository.IdempotencyRecordRepository;
import com.dukaanpe.payment.repository.PaymentTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private static final String OP_INITIATE_PAYMENT = "INITIATE_PAYMENT";

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final IdempotencySupport idempotencySupport;

    @Override
    public PaymentTransactionResponse initiatePayment(InitiatePaymentRequest request, String idempotencyKey) {
        String normalizedKey = idempotencySupport.normalizeKey(idempotencyKey);
        String payloadHash = idempotencySupport.hashPayload(request);

        if (normalizedKey != null) {
            IdempotencyRecord record = idempotencyRecordRepository
                .findByIdempotencyKeyAndOperation(normalizedKey, OP_INITIATE_PAYMENT)
                .orElse(null);
            if (record != null) {
                if (!record.getRequestHash().equals(payloadHash)) {
                    throw new InvalidPaymentStateException("Idempotency key reused with a different request payload");
                }
                return toResponse(loadByTransactionId(record.getResourceId()));
            }
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
            .storeId(request.getStoreId())
            .transactionId("TXN-" + UUID.randomUUID().toString().replace("-", ""))
            .billId(request.getBillId())
            .udharEntryId(request.getUdharEntryId())
            .amount(request.getAmount())
            .paymentMode(request.getPaymentMode())
            .paymentStatus(PaymentStatus.INITIATED)
            .upiId(request.getUpiId())
            .payerName(request.getPayerName())
            .payerPhone(request.getPayerPhone())
            .description(request.getDescription())
            .terminalId(normalizeTerminal(request.getTerminalId()))
            .build();
        PaymentTransaction saved = paymentTransactionRepository.save(transaction);

        if (normalizedKey != null) {
            idempotencyRecordRepository.save(IdempotencyRecord.builder()
                .idempotencyKey(normalizedKey)
                .operation(OP_INITIATE_PAYMENT)
                .requestHash(payloadHash)
                .resourceId(saved.getTransactionId())
                .build());
        }

        return toResponse(saved);
    }

    @Override
    public PaymentTransactionResponse completePayment(String transactionId, CompletePaymentRequest request) {
        PaymentTransaction transaction = loadByTransactionId(transactionId);
        if (transaction.getPaymentStatus() != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException("Only INITIATED transactions can be completed");
        }
        transaction.setPaymentStatus(PaymentStatus.COMPLETED);
        transaction.setUpiReference(request.getUpiReference());
        transaction.setCardLastFour(request.getCardLastFour());
        transaction.setFailureReason(null);
        return toResponse(paymentTransactionRepository.save(transaction));
    }

    @Override
    public PaymentTransactionResponse failPayment(String transactionId, FailPaymentRequest request) {
        PaymentTransaction transaction = loadByTransactionId(transactionId);
        if (transaction.getPaymentStatus() != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException("Only INITIATED transactions can be failed");
        }
        transaction.setPaymentStatus(PaymentStatus.FAILED);
        transaction.setFailureReason(request.getReason());
        return toResponse(paymentTransactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTransactionResponse getByTransactionId(String transactionId) {
        return toResponse(loadByTransactionId(transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentTransactionResponse> listTransactions(Long storeId, LocalDate date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PaymentTransaction> payments;

        if (date == null) {
            payments = paymentTransactionRepository.findByStoreId(storeId, pageable);
        } else {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();
            payments = paymentTransactionRepository.findByStoreIdAndCreatedAtBetween(storeId, from, to, pageable);
        }

        return PagedResponse.<PaymentTransactionResponse>builder()
            .content(payments.getContent().stream().map(this::toResponse).toList())
            .pageNumber(payments.getNumber())
            .pageSize(payments.getSize())
            .totalElements(payments.getTotalElements())
            .totalPages(payments.getTotalPages())
            .last(payments.isLast())
            .build();
    }

    @Override
    public PaymentTransactionResponse refundPayment(String transactionId, RefundPaymentRequest request) {
        PaymentTransaction transaction = loadByTransactionId(transactionId);
        if (transaction.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException("Only COMPLETED transactions can be refunded");
        }

        BigDecimal refundAmount = request.getRefundAmount() == null ? transaction.getAmount() : request.getRefundAmount();
        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new InvalidPaymentStateException("refundAmount cannot exceed original transaction amount");
        }

        transaction.setPaymentStatus(PaymentStatus.REFUNDED);
        transaction.setFailureReason("Refund reason: " + request.getReason() + " | Amount: " + refundAmount);
        return toResponse(paymentTransactionRepository.save(transaction));
    }

    private PaymentTransaction loadByTransactionId(String transactionId) {
        return paymentTransactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found: " + transactionId));
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction entity) {
        return PaymentTransactionResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .transactionId(entity.getTransactionId())
            .billId(entity.getBillId())
            .udharEntryId(entity.getUdharEntryId())
            .amount(entity.getAmount())
            .paymentMode(entity.getPaymentMode())
            .paymentStatus(entity.getPaymentStatus())
            .upiId(entity.getUpiId())
            .upiReference(entity.getUpiReference())
            .cardLastFour(entity.getCardLastFour())
            .payerName(entity.getPayerName())
            .payerPhone(entity.getPayerPhone())
            .description(entity.getDescription())
            .terminalId(entity.getTerminalId())
            .failureReason(entity.getFailureReason())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    private String normalizeTerminal(String terminalId) {
        if (terminalId == null || terminalId.isBlank()) {
            return "DEFAULT";
        }
        return terminalId.trim();
    }
}

