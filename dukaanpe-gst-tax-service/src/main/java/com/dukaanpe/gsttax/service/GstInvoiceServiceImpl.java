package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.GenerateGstInvoiceRequest;
import com.dukaanpe.gsttax.dto.GstInvoiceItemRequest;
import com.dukaanpe.gsttax.dto.GstInvoiceItemResponse;
import com.dukaanpe.gsttax.dto.GstInvoiceResponse;
import com.dukaanpe.gsttax.dto.PagedResponse;
import com.dukaanpe.gsttax.entity.GstInvoice;
import com.dukaanpe.gsttax.entity.GstInvoiceItem;
import com.dukaanpe.gsttax.entity.HsnMaster;
import com.dukaanpe.gsttax.exception.InvalidTaxOperationException;
import com.dukaanpe.gsttax.exception.ResourceNotFoundException;
import com.dukaanpe.gsttax.repository.GstInvoiceRepository;
import com.dukaanpe.gsttax.repository.HsnMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GstInvoiceServiceImpl implements GstInvoiceService {

    private final GstInvoiceRepository gstInvoiceRepository;
    private final HsnMasterRepository hsnMasterRepository;

    @Override
    public GstInvoiceResponse generate(GenerateGstInvoiceRequest request) {
        String invoiceNumber = request.getInvoiceNumber().trim();
        if (gstInvoiceRepository.existsByStoreIdAndInvoiceNumber(request.getStoreId(), invoiceNumber)) {
            throw new InvalidTaxOperationException("Invoice already exists for store: " + invoiceNumber);
        }

        GstInvoice invoice = GstInvoice.builder()
            .storeId(request.getStoreId())
            .invoiceNumber(invoiceNumber)
            .invoiceDate(request.getInvoiceDate())
            .customerName(request.getCustomerName())
            .customerGstin(request.getCustomerGstin())
            .placeOfSupply(request.getPlaceOfSupply())
            .intraState(request.getIntraState())
            .build();

        List<GstInvoiceItem> items = new ArrayList<>();
        BigDecimal taxableAmount = BigDecimal.ZERO;
        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;
        BigDecimal cessAmount = BigDecimal.ZERO;

        for (GstInvoiceItemRequest itemRequest : request.getItems()) {
            HsnMaster hsn = loadActiveHsn(itemRequest.getHsnCode().trim());
            TaxBreakup breakup = calculateTax(itemRequest.getTaxableValue(), hsn.getGstRate(), hsn.getCessRate(), request.getIntraState());

            GstInvoiceItem item = GstInvoiceItem.builder()
                .invoice(invoice)
                .hsnCode(hsn.getHsnCode())
                .itemDescription(itemRequest.getItemDescription().trim())
                .quantity(itemRequest.getQuantity())
                .taxableValue(scale(itemRequest.getTaxableValue()))
                .gstRate(scale(hsn.getGstRate()))
                .cessRate(scale(hsn.getCessRate()))
                .cgstAmount(breakup.cgstAmount)
                .sgstAmount(breakup.sgstAmount)
                .igstAmount(breakup.igstAmount)
                .cessAmount(breakup.cessAmount)
                .totalTax(breakup.totalTax)
                .lineTotal(scale(itemRequest.getTaxableValue().add(breakup.totalTax)))
                .build();
            items.add(item);

            taxableAmount = taxableAmount.add(item.getTaxableValue());
            cgstAmount = cgstAmount.add(item.getCgstAmount());
            sgstAmount = sgstAmount.add(item.getSgstAmount());
            igstAmount = igstAmount.add(item.getIgstAmount());
            cessAmount = cessAmount.add(item.getCessAmount());
        }

        BigDecimal totalTax = cgstAmount.add(sgstAmount).add(igstAmount).add(cessAmount);

        invoice.setTaxableAmount(scale(taxableAmount));
        invoice.setCgstAmount(scale(cgstAmount));
        invoice.setSgstAmount(scale(sgstAmount));
        invoice.setIgstAmount(scale(igstAmount));
        invoice.setCessAmount(scale(cessAmount));
        invoice.setTotalTaxAmount(scale(totalTax));
        invoice.setInvoiceTotal(scale(taxableAmount.add(totalTax)));
        invoice.setItems(items);

        return toResponse(gstInvoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public GstInvoiceResponse getById(Long id) {
        return toResponse(loadInvoice(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GstInvoiceResponse> listByDateRange(Long storeId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "invoiceDate").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<GstInvoice> invoices = gstInvoiceRepository.findByStoreIdAndInvoiceDateBetween(storeId, fromDate, toDate, pageable);

        return PagedResponse.<GstInvoiceResponse>builder()
            .content(invoices.getContent().stream().map(this::toResponse).toList())
            .pageNumber(invoices.getNumber())
            .pageSize(invoices.getSize())
            .totalElements(invoices.getTotalElements())
            .totalPages(invoices.getTotalPages())
            .last(invoices.isLast())
            .build();
    }

    private GstInvoice loadInvoice(Long id) {
        return gstInvoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GST invoice not found: " + id));
    }

    private HsnMaster loadActiveHsn(String hsnCode) {
        HsnMaster hsn = hsnMasterRepository.findByHsnCode(hsnCode)
            .orElseThrow(() -> new ResourceNotFoundException("HSN not found: " + hsnCode));
        if (!Boolean.TRUE.equals(hsn.getIsActive())) {
            throw new InvalidTaxOperationException("HSN is inactive: " + hsnCode);
        }
        return hsn;
    }

    private TaxBreakup calculateTax(BigDecimal taxableValue, BigDecimal gstRate, BigDecimal cessRate, boolean intraState) {
        BigDecimal taxable = scale(taxableValue);
        BigDecimal gstTax = scale(taxable.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        BigDecimal cessTax = scale(taxable.multiply(cessRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;
        if (intraState) {
            cgst = scale(gstTax.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP));
            sgst = scale(gstTax.subtract(cgst));
        } else {
            igst = gstTax;
        }

        return new TaxBreakup(cgst, sgst, igst, cessTax, scale(gstTax.add(cessTax)));
    }

    private GstInvoiceResponse toResponse(GstInvoice invoice) {
        return GstInvoiceResponse.builder()
            .id(invoice.getId())
            .storeId(invoice.getStoreId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .invoiceDate(invoice.getInvoiceDate())
            .customerName(invoice.getCustomerName())
            .customerGstin(invoice.getCustomerGstin())
            .placeOfSupply(invoice.getPlaceOfSupply())
            .intraState(invoice.getIntraState())
            .taxableAmount(invoice.getTaxableAmount())
            .cgstAmount(invoice.getCgstAmount())
            .sgstAmount(invoice.getSgstAmount())
            .igstAmount(invoice.getIgstAmount())
            .cessAmount(invoice.getCessAmount())
            .totalTaxAmount(invoice.getTotalTaxAmount())
            .invoiceTotal(invoice.getInvoiceTotal())
            .items(invoice.getItems().stream().map(this::toItemResponse).toList())
            .createdAt(invoice.getCreatedAt())
            .build();
    }

    private GstInvoiceItemResponse toItemResponse(GstInvoiceItem item) {
        return GstInvoiceItemResponse.builder()
            .id(item.getId())
            .hsnCode(item.getHsnCode())
            .itemDescription(item.getItemDescription())
            .quantity(item.getQuantity())
            .taxableValue(item.getTaxableValue())
            .gstRate(item.getGstRate())
            .cessRate(item.getCessRate())
            .cgstAmount(item.getCgstAmount())
            .sgstAmount(item.getSgstAmount())
            .igstAmount(item.getIgstAmount())
            .cessAmount(item.getCessAmount())
            .totalTax(item.getTotalTax())
            .lineTotal(item.getLineTotal())
            .build();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record TaxBreakup(
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal cessAmount,
        BigDecimal totalTax
    ) {
    }
}

