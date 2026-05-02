package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.GstReturnResponse;
import com.dukaanpe.gsttax.dto.PrepareGstReturnRequest;
import com.dukaanpe.gsttax.entity.GstInvoice;
import com.dukaanpe.gsttax.entity.GstReturn;
import com.dukaanpe.gsttax.exception.InvalidTaxOperationException;
import com.dukaanpe.gsttax.exception.ResourceNotFoundException;
import com.dukaanpe.gsttax.repository.GstInvoiceRepository;
import com.dukaanpe.gsttax.repository.GstReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GstReturnServiceImpl implements GstReturnService {

    private final GstReturnRepository gstReturnRepository;
    private final GstInvoiceRepository gstInvoiceRepository;

    @Override
    public GstReturnResponse prepare(PrepareGstReturnRequest request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new InvalidTaxOperationException("periodEnd must be on or after periodStart");
        }

        List<GstInvoice> invoices = gstInvoiceRepository.findByStoreIdAndInvoiceDateBetween(
            request.getStoreId(),
            request.getPeriodStart(),
            request.getPeriodEnd()
        );

        BigDecimal taxable = invoices.stream()
            .map(GstInvoice::getTaxableAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = invoices.stream()
            .map(GstInvoice::getTotalTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        GstReturn gstReturn = GstReturn.builder()
            .storeId(request.getStoreId())
            .returnType(request.getReturnType())
            .periodStart(request.getPeriodStart())
            .periodEnd(request.getPeriodEnd())
            .totalInvoices(invoices.size())
            .taxableAmount(scale(taxable))
            .totalTaxAmount(scale(totalTax))
            .totalTaxLiability(scale(totalTax))
            .build();

        return toResponse(gstReturnRepository.save(gstReturn));
    }

    @Override
    @Transactional(readOnly = true)
    public GstReturnResponse getById(Long id) {
        GstReturn gstReturn = gstReturnRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("GST return not found: " + id));
        return toResponse(gstReturn);
    }

    private GstReturnResponse toResponse(GstReturn gstReturn) {
        return GstReturnResponse.builder()
            .id(gstReturn.getId())
            .storeId(gstReturn.getStoreId())
            .returnType(gstReturn.getReturnType())
            .periodStart(gstReturn.getPeriodStart())
            .periodEnd(gstReturn.getPeriodEnd())
            .totalInvoices(gstReturn.getTotalInvoices())
            .taxableAmount(gstReturn.getTaxableAmount())
            .totalTaxAmount(gstReturn.getTotalTaxAmount())
            .totalTaxLiability(gstReturn.getTotalTaxLiability())
            .generatedAt(gstReturn.getGeneratedAt())
            .build();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

