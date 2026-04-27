package com.dukaanpe.udhar.service;

import com.dukaanpe.udhar.dto.CreateKhataCustomerRequest;
import com.dukaanpe.udhar.dto.CreateReminderRequest;
import com.dukaanpe.udhar.dto.CreateSettlementRequest;
import com.dukaanpe.udhar.dto.CreditRequest;
import com.dukaanpe.udhar.dto.KhataCustomerResponse;
import com.dukaanpe.udhar.dto.PagedResponse;
import com.dukaanpe.udhar.dto.PaymentRequest;
import com.dukaanpe.udhar.dto.ReminderResponse;
import com.dukaanpe.udhar.dto.SettlementMonthlyReportResponse;
import com.dukaanpe.udhar.dto.SettlementResponse;
import com.dukaanpe.udhar.dto.UdharEntryResponse;
import com.dukaanpe.udhar.dto.UdharSummaryResponse;
import com.dukaanpe.udhar.dto.UpdateKhataCustomerRequest;
import com.dukaanpe.udhar.entity.EntryType;
import com.dukaanpe.udhar.entity.KhataCustomer;
import com.dukaanpe.udhar.entity.PaymentReminder;
import com.dukaanpe.udhar.entity.SettlementRecord;
import com.dukaanpe.udhar.entity.UdharEntry;
import com.dukaanpe.udhar.exception.ResourceNotFoundException;
import com.dukaanpe.udhar.repository.KhataCustomerRepository;
import com.dukaanpe.udhar.repository.PaymentReminderRepository;
import com.dukaanpe.udhar.repository.SettlementRecordRepository;
import com.dukaanpe.udhar.repository.UdharEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UdharKhataServiceImpl implements UdharKhataService {

    private final KhataCustomerRepository khataCustomerRepository;
    private final UdharEntryRepository udharEntryRepository;
    private final PaymentReminderRepository paymentReminderRepository;
    private final SettlementRecordRepository settlementRecordRepository;

    @Override
    @Transactional
    public KhataCustomerResponse addCustomer(CreateKhataCustomerRequest request) {
        KhataCustomer customer = KhataCustomer.builder()
            .storeId(request.getStoreId())
            .customerName(request.getCustomerName())
            .customerPhone(request.getCustomerPhone())
            .address(request.getAddress())
            .creditLimit(request.getCreditLimit() != null ? request.getCreditLimit() : new BigDecimal("5000"))
            .notes(request.getNotes())
            .build();
        return toCustomerResponse(khataCustomerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<KhataCustomerResponse> listCustomers(Long storeId, int page, int size) {
        Page<KhataCustomer> customerPage = khataCustomerRepository.findByStoreIdAndIsActiveTrue(
            storeId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );
        return toCustomerPagedResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public KhataCustomerResponse getCustomer(Long id) {
        return toCustomerResponse(getActiveCustomer(id));
    }

    @Override
    @Transactional
    public KhataCustomerResponse updateCustomer(Long id, UpdateKhataCustomerRequest request) {
        KhataCustomer customer = getActiveCustomer(id);
        customer.setCustomerName(request.getCustomerName());
        customer.setCustomerPhone(request.getCustomerPhone());
        customer.setAddress(request.getAddress());
        customer.setNotes(request.getNotes());
        if (request.getCreditLimit() != null) {
            customer.setCreditLimit(request.getCreditLimit());
        }
        return toCustomerResponse(khataCustomerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deactivateCustomer(Long id) {
        KhataCustomer customer = getActiveCustomer(id);
        customer.setIsActive(false);
        khataCustomerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<KhataCustomerResponse> searchCustomers(Long storeId, String query, int page, int size) {
        Page<KhataCustomer> customerPage = khataCustomerRepository
            .findByStoreIdAndIsActiveTrueAndCustomerNameContainingIgnoreCaseOrStoreIdAndIsActiveTrueAndCustomerPhoneContainingIgnoreCase(
                storeId,
                query,
                storeId,
                query,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
            );
        return toCustomerPagedResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhataCustomerResponse> topDefaulters(Long storeId) {
        return khataCustomerRepository.findTop10ByStoreIdAndIsActiveTrueOrderByTotalOutstandingDesc(storeId)
            .stream()
            .map(this::toCustomerResponse)
            .toList();
    }

    @Override
    @Transactional
    public UdharEntryResponse giveCredit(CreditRequest request) {
        KhataCustomer customer = resolveCustomerForCredit(request);
        BigDecimal previousOutstanding = customer.getTotalOutstanding();
        BigDecimal newOutstanding = previousOutstanding.add(request.getAmount());

        customer.setTotalOutstanding(newOutstanding);
        customer.setTotalCreditGiven(customer.getTotalCreditGiven().add(request.getAmount()));
        khataCustomerRepository.save(customer);

        UdharEntry entry = UdharEntry.builder()
            .storeId(request.getStoreId())
            .khataCustomer(customer)
            .billId(request.getBillId())
            .entryType(EntryType.CREDIT_GIVEN)
            .amount(request.getAmount())
            .runningBalance(newOutstanding)
            .description(request.getDescription())
            .itemsSummary(request.getItemsSummary())
            .dueDate(request.getDueDate())
            .createdBy(request.getCreatedBy())
            .build();

        UdharEntry saved = udharEntryRepository.save(entry);
        return toEntryResponse(saved, newOutstanding.compareTo(customer.getCreditLimit()) > 0);
    }

    @Override
    @Transactional
    public UdharEntryResponse recordPayment(PaymentRequest request) {
        KhataCustomer customer = getActiveCustomer(request.getKhataCustomerId());
        BigDecimal previousOutstanding = customer.getTotalOutstanding();

        BigDecimal appliedAmount = request.getAmount().min(previousOutstanding);
        BigDecimal newOutstanding = previousOutstanding.subtract(appliedAmount);

        customer.setTotalOutstanding(newOutstanding);
        customer.setTotalCollected(customer.getTotalCollected().add(appliedAmount));

        int nextScore = Math.min(10, customer.getTrustScore() + 1);
        customer.setTrustScore(nextScore);
        khataCustomerRepository.save(customer);

        EntryType entryType = newOutstanding.compareTo(BigDecimal.ZERO) == 0
            ? EntryType.PAYMENT_RECEIVED
            : EntryType.PARTIAL_PAYMENT;

        UdharEntry entry = UdharEntry.builder()
            .storeId(request.getStoreId())
            .khataCustomer(customer)
            .entryType(entryType)
            .amount(appliedAmount)
            .runningBalance(newOutstanding)
            .description(request.getDescription())
            .paymentMode(request.getPaymentMode())
            .referenceNumber(request.getReferenceNumber())
            .createdBy(request.getCreatedBy())
            .build();

        return toEntryResponse(udharEntryRepository.save(entry), false);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UdharEntryResponse> listEntries(Long customerId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        getActiveCustomer(customerId);
        LocalDate start = fromDate != null ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = toDate != null ? toDate : LocalDate.now().plusYears(10);

        Page<UdharEntry> entries = udharEntryRepository.findByKhataCustomerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            customerId,
            start.atStartOfDay(),
            end.atTime(23, 59, 59),
            PageRequest.of(page, size)
        );

        return PagedResponse.<UdharEntryResponse>builder()
            .content(entries.getContent().stream().map(entry -> toEntryResponse(entry, false)).toList())
            .pageNumber(entries.getNumber())
            .pageSize(entries.getSize())
            .totalElements(entries.getTotalElements())
            .totalPages(entries.getTotalPages())
            .last(entries.isLast())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UdharSummaryResponse summary(Long storeId) {
        List<KhataCustomer> customers = khataCustomerRepository.findByStoreIdAndIsActiveTrue(storeId);

        BigDecimal totalOutstanding = customers.stream()
            .map(KhataCustomer::getTotalOutstanding)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCreditGiven = customers.stream()
            .map(KhataCustomer::getTotalCreditGiven)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = customers.stream()
            .map(KhataCustomer::getTotalCollected)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UdharSummaryResponse.builder()
            .storeId(storeId)
            .totalOutstanding(totalOutstanding)
            .totalCreditGiven(totalCreditGiven)
            .totalCollected(totalCollected)
            .activeCustomers(customers.size())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UdharEntryResponse> overdue(Long storeId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = toDate != null ? toDate : LocalDate.now();

        Page<UdharEntry> entries = udharEntryRepository.findByStoreIdAndDueDateBetweenAndRunningBalanceGreaterThanOrderByDueDateAsc(
            storeId,
            start,
            end,
            BigDecimal.ZERO,
            PageRequest.of(page, size)
        );

        return PagedResponse.<UdharEntryResponse>builder()
            .content(entries.getContent().stream().map(entry -> toEntryResponse(entry, false)).toList())
            .pageNumber(entries.getNumber())
            .pageSize(entries.getSize())
            .totalElements(entries.getTotalElements())
            .totalPages(entries.getTotalPages())
            .last(entries.isLast())
            .build();
    }

    @Override
    @Transactional
    public ReminderResponse createReminder(CreateReminderRequest request) {
        KhataCustomer customer = getActiveCustomer(request.getKhataCustomerId());
        UdharEntry entry = null;
        if (request.getUdharEntryId() != null) {
            entry = udharEntryRepository.findById(request.getUdharEntryId())
                .orElseThrow(() -> new ResourceNotFoundException("Udhar entry not found with id: " + request.getUdharEntryId()));
        }

        PaymentReminder reminder = PaymentReminder.builder()
            .udharEntry(entry)
            .khataCustomer(customer)
            .storeId(request.getStoreId())
            .reminderDate(request.getReminderDate())
            .reminderType(request.getReminderType())
            .message(request.getMessage())
            .build();

        return toReminderResponse(paymentReminderRepository.save(reminder));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReminderResponse> pendingReminders(Long storeId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusMonths(1);
        LocalDate end = toDate != null ? toDate : LocalDate.now().plusMonths(1);

        Page<PaymentReminder> reminderPage = paymentReminderRepository.findByStoreIdAndIsSentFalseAndReminderDateBetweenOrderByReminderDateAsc(
            storeId,
            start,
            end,
            PageRequest.of(page, size)
        );
        return toReminderPagedResponse(reminderPage);
    }

    @Override
    @Transactional
    public ReminderResponse markReminderSent(Long reminderId) {
        PaymentReminder reminder = paymentReminderRepository.findById(reminderId)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));
        reminder.setIsSent(true);
        reminder.setSentAt(LocalDateTime.now());
        return toReminderResponse(paymentReminderRepository.save(reminder));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReminderResponse> reminderHistory(Long customerId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        getActiveCustomer(customerId);
        LocalDate start = fromDate != null ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = toDate != null ? toDate : LocalDate.now().plusYears(10);

        Page<PaymentReminder> reminderPage = paymentReminderRepository.findByKhataCustomerIdAndReminderDateBetweenOrderByCreatedAtDesc(
            customerId,
            start,
            end,
            PageRequest.of(page, size)
        );
        return toReminderPagedResponse(reminderPage);
    }

    @Override
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request) {
        KhataCustomer customer = getActiveCustomer(request.getKhataCustomerId());
        BigDecimal outstandingBefore = customer.getTotalOutstanding();
        BigDecimal discount = request.getDiscountGiven() == null ? BigDecimal.ZERO : request.getDiscountGiven();
        BigDecimal totalReduction = request.getAmountSettled().add(discount);
        BigDecimal updatedOutstanding = outstandingBefore.subtract(totalReduction);
        if (updatedOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            updatedOutstanding = BigDecimal.ZERO;
        }

        customer.setTotalOutstanding(updatedOutstanding);
        customer.setTotalCollected(customer.getTotalCollected().add(request.getAmountSettled()));
        khataCustomerRepository.save(customer);

        SettlementRecord record = SettlementRecord.builder()
            .khataCustomer(customer)
            .storeId(request.getStoreId())
            .settlementDate(request.getSettlementDate())
            .totalOutstandingBefore(outstandingBefore)
            .amountSettled(request.getAmountSettled())
            .discountGiven(discount)
            .settlementMode(request.getSettlementMode())
            .notes(request.getNotes())
            .build();

        return toSettlementResponse(settlementRecordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SettlementResponse> listSettlements(Long customerId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        getActiveCustomer(customerId);
        LocalDate start = fromDate != null ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = toDate != null ? toDate : LocalDate.now().plusYears(10);

        Page<SettlementRecord> pageResult = settlementRecordRepository.findByKhataCustomerIdAndSettlementDateBetweenOrderBySettlementDateDesc(
            customerId,
            start,
            end,
            PageRequest.of(page, size)
        );
        return toSettlementPagedResponse(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementMonthlyReportResponse settlementReport(Long storeId, String month, int page, int size) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);
        } catch (Exception ex) {
            throw new IllegalArgumentException("month must be in YYYY-MM format");
        }

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        Page<SettlementRecord> pageResult = settlementRecordRepository.findByStoreIdAndSettlementDateBetweenOrderBySettlementDateDesc(
            storeId,
            start,
            end,
            PageRequest.of(page, size)
        );

        PagedResponse<SettlementResponse> settlements = toSettlementPagedResponse(pageResult);

        BigDecimal totalAmountSettled = pageResult.getContent().stream()
            .map(SettlementRecord::getAmountSettled)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = pageResult.getContent().stream()
            .map(SettlementRecord::getDiscountGiven)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SettlementMonthlyReportResponse.builder()
            .storeId(storeId)
            .month(month)
            .totalAmountSettled(totalAmountSettled)
            .totalDiscountGiven(totalDiscount)
            .totalRecords(pageResult.getTotalElements())
            .settlements(settlements)
            .build();
    }

    private KhataCustomer resolveCustomerForCredit(CreditRequest request) {
        if (request.getKhataCustomerId() != null) {
            return getActiveCustomer(request.getKhataCustomerId());
        }
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            return khataCustomerRepository.findByStoreIdAndCustomerPhoneAndIsActiveTrue(request.getStoreId(), request.getCustomerPhone())
                .orElseThrow(() -> new ResourceNotFoundException("Khata customer not found for phone: " + request.getCustomerPhone()));
        }
        throw new IllegalArgumentException("Either khataCustomerId or customerPhone is required");
    }

    private KhataCustomer getActiveCustomer(Long id) {
        KhataCustomer customer = khataCustomerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Khata customer not found with id: " + id));
        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            throw new ResourceNotFoundException("Khata customer is inactive with id: " + id);
        }
        return customer;
    }

    private PagedResponse<KhataCustomerResponse> toCustomerPagedResponse(Page<KhataCustomer> page) {
        return PagedResponse.<KhataCustomerResponse>builder()
            .content(page.getContent().stream().map(this::toCustomerResponse).toList())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

    private PagedResponse<ReminderResponse> toReminderPagedResponse(Page<PaymentReminder> page) {
        return PagedResponse.<ReminderResponse>builder()
            .content(page.getContent().stream().map(this::toReminderResponse).toList())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

    private PagedResponse<SettlementResponse> toSettlementPagedResponse(Page<SettlementRecord> page) {
        return PagedResponse.<SettlementResponse>builder()
            .content(page.getContent().stream().map(this::toSettlementResponse).toList())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

    private KhataCustomerResponse toCustomerResponse(KhataCustomer customer) {
        return KhataCustomerResponse.builder()
            .id(customer.getId())
            .storeId(customer.getStoreId())
            .customerName(customer.getCustomerName())
            .customerPhone(customer.getCustomerPhone())
            .address(customer.getAddress())
            .creditLimit(customer.getCreditLimit())
            .totalOutstanding(customer.getTotalOutstanding())
            .totalCreditGiven(customer.getTotalCreditGiven())
            .totalCollected(customer.getTotalCollected())
            .trustScore(customer.getTrustScore())
            .isActive(customer.getIsActive())
            .notes(customer.getNotes())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }

    private ReminderResponse toReminderResponse(PaymentReminder reminder) {
        return ReminderResponse.builder()
            .id(reminder.getId())
            .udharEntryId(reminder.getUdharEntry() != null ? reminder.getUdharEntry().getId() : null)
            .khataCustomerId(reminder.getKhataCustomer().getId())
            .customerName(reminder.getKhataCustomer().getCustomerName())
            .storeId(reminder.getStoreId())
            .reminderDate(reminder.getReminderDate())
            .reminderType(reminder.getReminderType())
            .message(reminder.getMessage())
            .isSent(reminder.getIsSent())
            .sentAt(reminder.getSentAt())
            .createdAt(reminder.getCreatedAt())
            .build();
    }

    private SettlementResponse toSettlementResponse(SettlementRecord record) {
        return SettlementResponse.builder()
            .id(record.getId())
            .khataCustomerId(record.getKhataCustomer().getId())
            .customerName(record.getKhataCustomer().getCustomerName())
            .storeId(record.getStoreId())
            .settlementDate(record.getSettlementDate())
            .totalOutstandingBefore(record.getTotalOutstandingBefore())
            .amountSettled(record.getAmountSettled())
            .discountGiven(record.getDiscountGiven())
            .settlementMode(record.getSettlementMode())
            .notes(record.getNotes())
            .createdAt(record.getCreatedAt())
            .build();
    }

    private UdharEntryResponse toEntryResponse(UdharEntry entry, boolean creditLimitBreached) {
        return UdharEntryResponse.builder()
            .id(entry.getId())
            .storeId(entry.getStoreId())
            .khataCustomerId(entry.getKhataCustomer().getId())
            .customerName(entry.getKhataCustomer().getCustomerName())
            .billId(entry.getBillId())
            .entryType(entry.getEntryType())
            .amount(entry.getAmount())
            .runningBalance(entry.getRunningBalance())
            .description(entry.getDescription())
            .itemsSummary(entry.getItemsSummary())
            .dueDate(entry.getDueDate())
            .paymentMode(entry.getPaymentMode())
            .referenceNumber(entry.getReferenceNumber())
            .createdBy(entry.getCreatedBy())
            .createdAt(entry.getCreatedAt())
            .creditLimitBreached(creditLimitBreached)
            .build();
    }
}

