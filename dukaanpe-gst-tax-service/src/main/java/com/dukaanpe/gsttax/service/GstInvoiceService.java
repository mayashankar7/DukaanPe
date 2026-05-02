package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.GenerateGstInvoiceRequest;
import com.dukaanpe.gsttax.dto.GstInvoiceResponse;
import com.dukaanpe.gsttax.dto.PagedResponse;

import java.time.LocalDate;

public interface GstInvoiceService {

    GstInvoiceResponse generate(GenerateGstInvoiceRequest request);

    GstInvoiceResponse getById(Long id);

    PagedResponse<GstInvoiceResponse> listByDateRange(Long storeId, LocalDate fromDate, LocalDate toDate, int page, int size);
}

