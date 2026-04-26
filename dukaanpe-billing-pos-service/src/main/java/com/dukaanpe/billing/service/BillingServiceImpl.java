package com.dukaanpe.billing.service;

import com.dukaanpe.billing.dto.BillItemRequest;
import com.dukaanpe.billing.dto.BillItemResponse;
import com.dukaanpe.billing.dto.BillResponse;
import com.dukaanpe.billing.dto.BillingSummaryResponse;
import com.dukaanpe.billing.dto.CalculateBillRequest;
import com.dukaanpe.billing.dto.CalculateBillResponse;
import com.dukaanpe.billing.dto.CreateBillRequest;
import com.dukaanpe.billing.dto.PagedResponse;
import com.dukaanpe.billing.dto.UdharCreditRequest;
import com.dukaanpe.billing.entity.Bill;
import com.dukaanpe.billing.entity.BillItem;
import com.dukaanpe.billing.entity.BillStatus;
import com.dukaanpe.billing.entity.PaymentMode;
import com.dukaanpe.billing.exception.ResourceNotFoundException;
import com.dukaanpe.billing.repository.BillRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final BillRepository billRepository;
    private final RestTemplate restTemplate;
    private final AtomicInteger sequence = new AtomicInteger(0);

    @Value("${billing.udhar.enabled:true}")
    private boolean udharHookEnabled;

    @Value("${billing.udhar.base-url:http://localhost:8085}")
    private String udharBaseUrl;

    @Value("${billing.supplier-hook.enabled:false}")
    private boolean supplierHookEnabled;

    @Value("${billing.supplier-hook.base-url:http://localhost:8086}")
    private String supplierHookBaseUrl;

    @Value("${billing.supplier-hook.auto-suggest-limit:10}")
    private int supplierHookLimit;

    @Override
    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        CalculateBillResponse calculated = calculateFromItems(request.getItems());

        Bill bill = Bill.builder()
            .storeId(request.getStoreId())
            .billNumber(generateBillNumber(request.getStoreId()))
            .customerPhone(request.getCustomerPhone())
            .customerName(request.getCustomerName())
            .subtotal(calculated.getSubtotal())
            .totalDiscount(calculated.getTotalDiscount())
            .totalGst(calculated.getTotalGst())
            .cgst(calculated.getCgst())
            .sgst(calculated.getSgst())
            .igst(BigDecimal.ZERO)
            .roundOff(calculated.getRoundOff())
            .grandTotal(calculated.getGrandTotal())
            .paymentMode(request.getPaymentMode())
            .cashAmount(defaultAmount(request.getCashAmount()))
            .upiAmount(defaultAmount(request.getUpiAmount()))
            .cardAmount(defaultAmount(request.getCardAmount()))
            .creditAmount(defaultAmount(request.getCreditAmount()))
            .isCredit(request.getPaymentMode() == PaymentMode.CREDIT)
            .isGstBill(Boolean.TRUE.equals(request.getIsGstBill()))
            .gstinCustomer(request.getGstinCustomer())
            .status(BillStatus.COMPLETED)
            .notes(request.getNotes())
            .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
            .build();

        List<BillItem> items = mapToItems(bill, request.getItems());
        bill.setItems(items);
        Bill savedBill = billRepository.save(bill);
        triggerUdharCreditHook(savedBill, request);
        triggerSupplierLowStockHook(savedBill);
        return toResponse(savedBill);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BillResponse> listBills(Long storeId, LocalDate date, int page, int size) {
        Page<Bill> billPage;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "billingDate"));
        if (date != null) {
            billPage = billRepository.findByStoreIdAndBillingDateBetweenOrderByBillingDateDesc(
                storeId,
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX),
                pageRequest
            );
        } else {
            billPage = billRepository.findByStoreIdOrderByBillingDateDesc(storeId, pageRequest);
        }
        return toPagedResponse(billPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBill(Long id) {
        Bill bill = billRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        return toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with number: " + billNumber));
        return toResponse(bill);
    }

    @Override
    @Transactional
    public BillResponse cancelBill(Long id) {
        Bill bill = billRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        bill.setStatus(BillStatus.CANCELLED);
        return toResponse(billRepository.save(bill));
    }

    @Override
    @Transactional(readOnly = true)
    public BillingSummaryResponse todaySummary(Long storeId) {
        LocalDate today = LocalDate.now();
        List<Bill> bills = billRepository.findByStoreIdAndBillingDateBetween(
            storeId,
            today.atStartOfDay(),
            today.atTime(LocalTime.MAX)
        );

        BigDecimal revenue = bills.stream().map(Bill::getGrandTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = bills.stream().map(Bill::getTotalGst).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = bills.stream().map(Bill::getTotalDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long total = bills.size();
        BigDecimal avg = total == 0 ? BigDecimal.ZERO : revenue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return BillingSummaryResponse.builder()
            .storeId(storeId)
            .date(today.toString())
            .totalBills(total)
            .totalRevenue(revenue)
            .totalTax(tax)
            .totalDiscount(discount)
            .averageBillValue(avg)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BillResponse> searchBills(Long storeId, String query, int page, int size) {
        Page<Bill> billPage = billRepository
            .findByStoreIdAndBillNumberContainingIgnoreCaseOrStoreIdAndCustomerNameContainingIgnoreCaseOrderByBillingDateDesc(
                storeId,
                query,
                storeId,
                query,
                PageRequest.of(page, size)
            );
        return toPagedResponse(billPage);
    }

    @Override
    public CalculateBillResponse calculate(CalculateBillRequest request) {
        return calculateFromItems(request.getItems());
    }

    private CalculateBillResponse calculateFromItems(List<BillItemRequest> requests) {
        List<BillItemResponse> items = requests.stream().map(this::calculateItem).toList();

        BigDecimal subtotal = items.stream().map(BillItemResponse::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
            .subtract(items.stream().map(BillItemResponse::getGstAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal totalDiscount = items.stream().map(BillItemResponse::getDiscountAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGst = items.stream().map(BillItemResponse::getGstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cgst = items.stream().map(BillItemResponse::getCgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sgst = items.stream().map(BillItemResponse::getSgstAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal computed = subtotal.add(totalGst).setScale(2, RoundingMode.HALF_UP);
        BigDecimal rounded = new BigDecimal(Math.round(computed.doubleValue())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal roundOff = rounded.subtract(computed).setScale(2, RoundingMode.HALF_UP);

        return CalculateBillResponse.builder()
            .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
            .totalDiscount(totalDiscount.setScale(2, RoundingMode.HALF_UP))
            .totalGst(totalGst.setScale(2, RoundingMode.HALF_UP))
            .cgst(cgst.setScale(2, RoundingMode.HALF_UP))
            .sgst(sgst.setScale(2, RoundingMode.HALF_UP))
            .roundOff(roundOff)
            .grandTotal(rounded)
            .items(items)
            .build();
    }

    private BillItemResponse calculateItem(BillItemRequest req) {
        BigDecimal discountPercent = defaultAmount(req.getDiscountPercent());
        BigDecimal gstRate = defaultAmount(req.getGstRate());

        BigDecimal base = req.getUnitPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        BigDecimal discountAmount = base.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal taxable = base.subtract(discountAmount);
        BigDecimal gstAmount = taxable.multiply(gstRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal cgstAmount = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal sgstAmount = gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = taxable.add(gstAmount);

        return BillItemResponse.builder()
            .productId(req.getProductId())
            .productName(req.getProductName())
            .hsnCode(req.getHsnCode())
            .quantity(req.getQuantity())
            .unit(req.getUnit())
            .unitPrice(req.getUnitPrice())
            .mrp(req.getMrp())
            .discountPercent(discountPercent)
            .discountAmount(discountAmount)
            .gstRate(gstRate)
            .gstAmount(gstAmount)
            .cgstAmount(cgstAmount)
            .sgstAmount(sgstAmount)
            .totalAmount(totalAmount)
            .build();
    }

    private List<BillItem> mapToItems(Bill bill, List<BillItemRequest> requests) {
        return requests.stream().map(req -> {
            BillItemResponse item = calculateItem(req);
            return BillItem.builder()
                .bill(bill)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .hsnCode(item.getHsnCode())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .mrp(item.getMrp())
                .discountPercent(item.getDiscountPercent())
                .discountAmount(item.getDiscountAmount())
                .gstRate(item.getGstRate())
                .gstAmount(item.getGstAmount())
                .cgstAmount(item.getCgstAmount())
                .sgstAmount(item.getSgstAmount())
                .totalAmount(item.getTotalAmount())
                .build();
        }).toList();
    }

    private BillResponse toResponse(Bill bill) {
        List<BillItemResponse> items = bill.getItems().stream().map(item -> BillItemResponse.builder()
            .id(item.getId())
            .productId(item.getProductId())
            .productName(item.getProductName())
            .hsnCode(item.getHsnCode())
            .quantity(item.getQuantity())
            .unit(item.getUnit())
            .unitPrice(item.getUnitPrice())
            .mrp(item.getMrp())
            .discountPercent(item.getDiscountPercent())
            .discountAmount(item.getDiscountAmount())
            .gstRate(item.getGstRate())
            .gstAmount(item.getGstAmount())
            .cgstAmount(item.getCgstAmount())
            .sgstAmount(item.getSgstAmount())
            .totalAmount(item.getTotalAmount())
            .build()).toList();

        return BillResponse.builder()
            .id(bill.getId())
            .storeId(bill.getStoreId())
            .billNumber(bill.getBillNumber())
            .customerPhone(bill.getCustomerPhone())
            .customerName(bill.getCustomerName())
            .billingDate(bill.getBillingDate())
            .subtotal(bill.getSubtotal())
            .totalDiscount(bill.getTotalDiscount())
            .totalGst(bill.getTotalGst())
            .cgst(bill.getCgst())
            .sgst(bill.getSgst())
            .igst(bill.getIgst())
            .roundOff(bill.getRoundOff())
            .grandTotal(bill.getGrandTotal())
            .paymentMode(bill.getPaymentMode())
            .cashAmount(bill.getCashAmount())
            .upiAmount(bill.getUpiAmount())
            .cardAmount(bill.getCardAmount())
            .creditAmount(bill.getCreditAmount())
            .isCredit(bill.getIsCredit())
            .isGstBill(bill.getIsGstBill())
            .gstinCustomer(bill.getGstinCustomer())
            .status(bill.getStatus())
            .notes(bill.getNotes())
            .createdBy(bill.getCreatedBy())
            .createdAt(bill.getCreatedAt())
            .items(items)
            .build();
    }

    private PagedResponse<BillResponse> toPagedResponse(Page<Bill> billPage) {
        return PagedResponse.<BillResponse>builder()
            .content(billPage.getContent().stream().map(this::toResponse).toList())
            .pageNumber(billPage.getNumber())
            .pageSize(billPage.getSize())
            .totalElements(billPage.getTotalElements())
            .totalPages(billPage.getTotalPages())
            .last(billPage.isLast())
            .build();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String generateBillNumber(Long storeId) {
        String prefix = storeId == 1L ? "RGS" : "S" + storeId;
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int seq = sequence.updateAndGet(value -> value >= 999 ? 1 : value + 1);
        return prefix + "-" + date + "-" + String.format("%03d", seq);
    }

    private void triggerUdharCreditHook(Bill savedBill, CreateBillRequest request) {
        if (!udharHookEnabled || savedBill.getPaymentMode() != PaymentMode.CREDIT) {
            return;
        }
        if (savedBill.getCustomerPhone() == null || savedBill.getCustomerPhone().isBlank()) {
            log.warn("Skipping udhar hook for bill {} because customer phone is missing", savedBill.getBillNumber());
            return;
        }

        UdharCreditRequest udharRequest = new UdharCreditRequest();
        udharRequest.setStoreId(savedBill.getStoreId());
        udharRequest.setCustomerPhone(savedBill.getCustomerPhone());
        udharRequest.setBillId(savedBill.getId());
        udharRequest.setAmount(savedBill.getCreditAmount().compareTo(BigDecimal.ZERO) > 0 ? savedBill.getCreditAmount() : savedBill.getGrandTotal());
        udharRequest.setDescription("Auto credit from billing bill " + savedBill.getBillNumber());
        udharRequest.setItemsSummary(buildItemsSummary(savedBill));
        udharRequest.setDueDate(LocalDate.now().plusDays(30));
        udharRequest.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "billing-service");

        try {
            restTemplate.postForEntity(udharBaseUrl + "/api/udhar/credit", udharRequest, Object.class);
        } catch (Exception ex) {
            // Keep billing resilient if udhar service is unavailable.
            log.warn("Udhar hook failed for bill {}: {}", savedBill.getBillNumber(), ex.getMessage());
        }
    }

    private String buildItemsSummary(Bill bill) {
        return bill.getItems().stream()
            .limit(4)
            .map(item -> item.getQuantity() + " x " + item.getProductName())
            .reduce((left, right) -> left + ", " + right)
            .orElse("Credit bill items");
    }

    private void triggerSupplierLowStockHook(Bill savedBill) {
        if (!supplierHookEnabled) {
            return;
        }
        try {
            String url = supplierHookBaseUrl + "/api/purchase-orders/auto-suggest?storeId={storeId}&limit={limit}";
            restTemplate.getForEntity(url, Object.class, savedBill.getStoreId(), Math.max(1, supplierHookLimit));
        } catch (Exception ex) {
            // Billing should not fail due to supplier side unavailability.
            log.warn("Supplier low-stock hook failed for bill {}: {}", savedBill.getBillNumber(), ex.getMessage());
        }
    }
}

