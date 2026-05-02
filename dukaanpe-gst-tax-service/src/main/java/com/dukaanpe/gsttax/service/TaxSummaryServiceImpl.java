package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.TaxRateBreakupResponse;
import com.dukaanpe.gsttax.dto.TaxSummaryResponse;
import com.dukaanpe.gsttax.entity.GstInvoice;
import com.dukaanpe.gsttax.entity.GstInvoiceItem;
import com.dukaanpe.gsttax.repository.GstInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxSummaryServiceImpl implements TaxSummaryService {

    private final GstInvoiceRepository gstInvoiceRepository;

    @Override
    public TaxSummaryResponse summarize(Long storeId, LocalDate fromDate, LocalDate toDate) {
        List<GstInvoice> invoices = gstInvoiceRepository.findByStoreIdAndInvoiceDateBetween(storeId, fromDate, toDate);

        BigDecimal taxable = sumInvoices(invoices, GstInvoice::getTaxableAmount);
        BigDecimal cgst = sumInvoices(invoices, GstInvoice::getCgstAmount);
        BigDecimal sgst = sumInvoices(invoices, GstInvoice::getSgstAmount);
        BigDecimal igst = sumInvoices(invoices, GstInvoice::getIgstAmount);
        BigDecimal cess = sumInvoices(invoices, GstInvoice::getCessAmount);
        BigDecimal totalTax = sumInvoices(invoices, GstInvoice::getTotalTaxAmount);
        BigDecimal totalInvoice = sumInvoices(invoices, GstInvoice::getInvoiceTotal);

        Map<BigDecimal, List<GstInvoiceItem>> byTaxRate = invoices.stream()
            .flatMap(invoice -> invoice.getItems().stream())
            .collect(Collectors.groupingBy(item -> scale(item.getGstRate())));

        List<TaxRateBreakupResponse> breakup = byTaxRate.entrySet().stream()
            .map(entry -> {
                BigDecimal taxableAmount = entry.getValue().stream()
                    .map(GstInvoiceItem::getTaxableValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal taxAmount = entry.getValue().stream()
                    .map(GstInvoiceItem::getTotalTax)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return TaxRateBreakupResponse.builder()
                    .gstRate(scale(entry.getKey()))
                    .taxableAmount(scale(taxableAmount))
                    .totalTaxAmount(scale(taxAmount))
                    .build();
            })
            .sorted(Comparator.comparing(TaxRateBreakupResponse::getGstRate))
            .toList();

        return TaxSummaryResponse.builder()
            .storeId(storeId)
            .fromDate(fromDate)
            .toDate(toDate)
            .invoiceCount(invoices.size())
            .taxableAmount(scale(taxable))
            .cgstAmount(scale(cgst))
            .sgstAmount(scale(sgst))
            .igstAmount(scale(igst))
            .cessAmount(scale(cess))
            .totalTaxAmount(scale(totalTax))
            .totalInvoiceAmount(scale(totalInvoice))
            .taxRateBreakup(breakup)
            .build();
    }

    private BigDecimal sumInvoices(List<GstInvoice> invoices, java.util.function.Function<GstInvoice, BigDecimal> mapper) {
        return invoices.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

