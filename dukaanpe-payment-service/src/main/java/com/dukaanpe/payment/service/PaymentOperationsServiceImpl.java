package com.dukaanpe.payment.service;

import com.dukaanpe.payment.dto.CashRegisterResponse;
import com.dukaanpe.payment.dto.CloseCashRegisterRequest;
import com.dukaanpe.payment.dto.CreateReconciliationRequest;
import com.dukaanpe.payment.dto.GenerateUpiLinkRequest;
import com.dukaanpe.payment.dto.GenerateUpiLinkResponse;
import com.dukaanpe.payment.dto.GenerateUpiQrResponse;
import com.dukaanpe.payment.dto.OpenCashRegisterRequest;
import com.dukaanpe.payment.dto.PaymentReconciliationResponse;
import com.dukaanpe.payment.dto.SaveUpiQrCodeRequest;
import com.dukaanpe.payment.dto.UpiQrCodeResponse;
import com.dukaanpe.payment.entity.DailyCashRegister;
import com.dukaanpe.payment.entity.IdempotencyRecord;
import com.dukaanpe.payment.entity.PaymentMode;
import com.dukaanpe.payment.entity.PaymentReconciliation;
import com.dukaanpe.payment.entity.PaymentStatus;
import com.dukaanpe.payment.entity.ReconciliationStatus;
import com.dukaanpe.payment.entity.UpiQrCode;
import com.dukaanpe.payment.exception.InvalidPaymentStateException;
import com.dukaanpe.payment.exception.ResourceNotFoundException;
import com.dukaanpe.payment.repository.DailyCashRegisterRepository;
import com.dukaanpe.payment.repository.IdempotencyRecordRepository;
import com.dukaanpe.payment.repository.PaymentReconciliationRepository;
import com.dukaanpe.payment.repository.PaymentTransactionRepository;
import com.dukaanpe.payment.repository.UpiQrCodeRepository;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentOperationsServiceImpl implements PaymentOperationsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String OP_CREATE_RECONCILIATION = "CREATE_RECONCILIATION";
    private static final String OP_OPEN_CASH_REGISTER = "OPEN_CASH_REGISTER";
    private static final String OP_CLOSE_CASH_REGISTER = "CLOSE_CASH_REGISTER";

    private final UpiQrCodeRepository upiQrCodeRepository;
    private final PaymentReconciliationRepository paymentReconciliationRepository;
    private final DailyCashRegisterRepository dailyCashRegisterRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final IdempotencySupport idempotencySupport;

    @Override
    public GenerateUpiLinkResponse generateUpiLink(GenerateUpiLinkRequest request) {
        String encodedName = URLEncoder.encode(request.getMerchantName(), StandardCharsets.UTF_8);
        String encodedDesc = URLEncoder.encode(safeString(request.getDescription()), StandardCharsets.UTF_8);
        String amount = request.getAmount() == null ? "" : request.getAmount().toPlainString();
        String link = "upi://pay?pa=" + request.getMerchantUpiId()
            + "&pn=" + encodedName
            + "&am=" + amount
            + "&cu=INR&tn=" + encodedDesc;

        return GenerateUpiLinkResponse.builder().upiLink(link).build();
    }

    @Override
    public GenerateUpiQrResponse generateUpiQr(GenerateUpiLinkRequest request) {
        String link = generateUpiLink(request).getUpiLink();
        return GenerateUpiQrResponse.builder().qrCodeData(link).build();
    }

    @Override
    public UpiQrCodeResponse saveUpiQrCode(SaveUpiQrCodeRequest request) {
        UpiQrCode entity = UpiQrCode.builder()
            .storeId(request.getStoreId())
            .merchantUpiId(request.getMerchantUpiId())
            .merchantName(request.getMerchantName())
            .qrCodeImageBase64(request.getQrCodeImageBase64())
            .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
            .build();
        return toResponse(upiQrCodeRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpiQrCodeResponse> getUpiQrCodes(Long storeId) {
        return upiQrCodeRepository.findByStoreIdOrderByCreatedAtDesc(storeId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public PaymentReconciliationResponse createReconciliation(CreateReconciliationRequest request, String idempotencyKey) {
        String normalizedKey = idempotencySupport.normalizeKey(idempotencyKey);
        String payloadHash = idempotencySupport.hashPayload(request);

        if (normalizedKey != null) {
            IdempotencyRecord record = idempotencyRecordRepository
                .findByIdempotencyKeyAndOperation(normalizedKey, OP_CREATE_RECONCILIATION)
                .orElse(null);
            if (record != null) {
                if (!record.getRequestHash().equals(payloadHash)) {
                    throw new InvalidPaymentStateException("Idempotency key reused with a different request payload");
                }
                PaymentReconciliation existing = paymentReconciliationRepository.findById(Long.parseLong(record.getResourceId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Reconciliation not found for idempotency replay"));
                return toResponse(existing);
            }
        }

        if (paymentReconciliationRepository.existsByStoreIdAndReconDate(request.getStoreId(), request.getReconDate())) {
            throw new InvalidPaymentStateException("Reconciliation already exists for store and date");
        }

        BigDecimal totalCash = safeMoney(request.getTotalCash());
        BigDecimal totalUpi = safeMoney(request.getTotalUpi());
        BigDecimal totalCard = safeMoney(request.getTotalCard());
        BigDecimal totalCollections = totalCash.add(totalUpi).add(totalCard);
        BigDecimal cashInHand = safeMoney(request.getCashInHand());
        BigDecimal discrepancy = cashInHand.subtract(totalCash);
        ReconciliationStatus status = discrepancy.compareTo(ZERO) == 0
            ? ReconciliationStatus.MATCHED
            : ReconciliationStatus.DISCREPANCY;

        PaymentReconciliation entity = PaymentReconciliation.builder()
            .storeId(request.getStoreId())
            .reconDate(request.getReconDate())
            .totalCash(totalCash)
            .totalUpi(totalUpi)
            .totalCard(totalCard)
            .totalCollections(totalCollections)
            .cashInHand(cashInHand)
            .discrepancy(discrepancy)
            .status(status)
            .notes(request.getNotes())
            .build();

        PaymentReconciliation saved = paymentReconciliationRepository.save(entity);

        if (normalizedKey != null) {
            idempotencyRecordRepository.save(IdempotencyRecord.builder()
                .idempotencyKey(normalizedKey)
                .operation(OP_CREATE_RECONCILIATION)
                .requestHash(payloadHash)
                .resourceId(String.valueOf(saved.getId()))
                .build());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentReconciliationResponse> getMonthlyReconciliation(Long storeId, String month) {
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to = from.plusMonths(1).minusDays(1);
        return paymentReconciliationRepository.findByStoreIdAndReconDateBetweenOrderByReconDateDesc(storeId, from, to)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    public CashRegisterResponse openCashRegister(OpenCashRegisterRequest request, String idempotencyKey) {
        String terminalId = normalizeTerminal(request.getTerminalId());
        String normalizedKey = idempotencySupport.normalizeKey(idempotencyKey);
        String payloadHash = idempotencySupport.hashPayload(request);

        if (normalizedKey != null) {
            IdempotencyRecord record = idempotencyRecordRepository
                .findByIdempotencyKeyAndOperation(normalizedKey, OP_OPEN_CASH_REGISTER)
                .orElse(null);
            if (record != null) {
                if (!record.getRequestHash().equals(payloadHash)) {
                    throw new InvalidPaymentStateException("Idempotency key reused with a different request payload");
                }
                DailyCashRegister existing = dailyCashRegisterRepository.findById(Long.parseLong(record.getResourceId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Cash register not found for idempotency replay"));
                return toResponse(existing);
            }
        }

        if (dailyCashRegisterRepository.existsByStoreIdAndTerminalIdAndRegisterDateAndIsClosedFalse(
            request.getStoreId(), terminalId, request.getRegisterDate())) {
            throw new InvalidPaymentStateException("Cash register already open for the requested date");
        }

        DailyCashRegister register = DailyCashRegister.builder()
            .storeId(request.getStoreId())
            .terminalId(terminalId)
            .registerDate(request.getRegisterDate())
            .openingBalance(request.getOpeningBalance())
            .totalCashReceived(ZERO)
            .totalCashPaid(ZERO)
            .closingBalance(request.getOpeningBalance())
            .isClosed(false)
            .build();

        DailyCashRegister saved = dailyCashRegisterRepository.save(register);
        if (normalizedKey != null) {
            idempotencyRecordRepository.save(IdempotencyRecord.builder()
                .idempotencyKey(normalizedKey)
                .operation(OP_OPEN_CASH_REGISTER)
                .requestHash(payloadHash)
                .resourceId(String.valueOf(saved.getId()))
                .build());
        }
        return toResponse(saved);
    }

    @Override
    public CashRegisterResponse closeCashRegister(CloseCashRegisterRequest request, String idempotencyKey) {
        String terminalId = normalizeTerminal(request.getTerminalId());
        String normalizedKey = idempotencySupport.normalizeKey(idempotencyKey);
        String payloadHash = idempotencySupport.hashPayload(request);

        if (normalizedKey != null) {
            IdempotencyRecord record = idempotencyRecordRepository
                .findByIdempotencyKeyAndOperation(normalizedKey, OP_CLOSE_CASH_REGISTER)
                .orElse(null);
            if (record != null) {
                if (!record.getRequestHash().equals(payloadHash)) {
                    throw new InvalidPaymentStateException("Idempotency key reused with a different request payload");
                }
                DailyCashRegister existing = dailyCashRegisterRepository.findById(Long.parseLong(record.getResourceId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Cash register not found for idempotency replay"));
                return toResponse(existing);
            }
        }

        DailyCashRegister register = dailyCashRegisterRepository.findTopByStoreIdAndTerminalIdAndIsClosedFalseOrderByRegisterDateDesc(
                request.getStoreId(), terminalId)
            .orElseThrow(() -> new ResourceNotFoundException("No open cash register found for storeId: " + request.getStoreId()));

        LocalDateTime from = register.getRegisterDate().atStartOfDay();
        LocalDateTime to = register.getRegisterDate().plusDays(1).atStartOfDay();

        BigDecimal totalCashReceived = paymentTransactionRepository.sumAmountByStoreAndDateAndModeAndStatusAndTerminal(
            register.getStoreId(), from, to, PaymentMode.CASH, PaymentStatus.COMPLETED, terminalId);
        BigDecimal totalCashPaid = safeMoney(request.getTotalCashPaid());
        BigDecimal closingBalance = safeMoney(register.getOpeningBalance()).add(totalCashReceived).subtract(totalCashPaid);
        BigDecimal actual = safeMoney(request.getActualCashInDrawer());

        register.setTotalCashReceived(totalCashReceived);
        register.setTotalCashPaid(totalCashPaid);
        register.setClosingBalance(closingBalance);
        register.setActualCashInDrawer(actual);
        register.setDifference(actual.subtract(closingBalance));
        register.setClosedBy(request.getClosedBy());
        register.setIsClosed(true);
        register.setUpdatedAt(LocalDateTime.now());

        DailyCashRegister saved = dailyCashRegisterRepository.save(register);
        if (normalizedKey != null) {
            idempotencyRecordRepository.save(IdempotencyRecord.builder()
                .idempotencyKey(normalizedKey)
                .operation(OP_CLOSE_CASH_REGISTER)
                .requestHash(payloadHash)
                .resourceId(String.valueOf(saved.getId()))
                .build());
        }
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashRegisterResponse getCurrentCashRegister(Long storeId, String terminalId) {
        DailyCashRegister register = dailyCashRegisterRepository
            .findTopByStoreIdAndTerminalIdAndIsClosedFalseOrderByRegisterDateDesc(storeId, normalizeTerminal(terminalId))
            .orElseThrow(() -> new ResourceNotFoundException("No open cash register found for storeId: " + storeId));
        return toResponse(register);
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? ZERO : amount;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String normalizeTerminal(String terminalId) {
        if (terminalId == null || terminalId.isBlank()) {
            return "DEFAULT";
        }
        return terminalId.trim();
    }

    private UpiQrCodeResponse toResponse(UpiQrCode entity) {
        return UpiQrCodeResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .merchantUpiId(entity.getMerchantUpiId())
            .merchantName(entity.getMerchantName())
            .qrCodeImageBase64(entity.getQrCodeImageBase64())
            .isDefault(entity.getIsDefault())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    private PaymentReconciliationResponse toResponse(PaymentReconciliation entity) {
        return PaymentReconciliationResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .reconDate(entity.getReconDate())
            .totalCash(entity.getTotalCash())
            .totalUpi(entity.getTotalUpi())
            .totalCard(entity.getTotalCard())
            .totalCollections(entity.getTotalCollections())
            .cashInHand(entity.getCashInHand())
            .discrepancy(entity.getDiscrepancy())
            .status(entity.getStatus())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    private CashRegisterResponse toResponse(DailyCashRegister entity) {
        return CashRegisterResponse.builder()
            .id(entity.getId())
            .storeId(entity.getStoreId())
            .terminalId(entity.getTerminalId())
            .registerDate(entity.getRegisterDate())
            .openingBalance(entity.getOpeningBalance())
            .totalCashReceived(entity.getTotalCashReceived())
            .totalCashPaid(entity.getTotalCashPaid())
            .closingBalance(entity.getClosingBalance())
            .actualCashInDrawer(entity.getActualCashInDrawer())
            .difference(entity.getDifference())
            .closedBy(entity.getClosedBy())
            .isClosed(entity.getIsClosed())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}

