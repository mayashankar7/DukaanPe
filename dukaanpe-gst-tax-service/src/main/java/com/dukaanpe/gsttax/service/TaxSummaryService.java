package com.dukaanpe.gsttax.service;

import com.dukaanpe.gsttax.dto.TaxSummaryResponse;

import java.time.LocalDate;

public interface TaxSummaryService {

    TaxSummaryResponse summarize(Long storeId, LocalDate fromDate, LocalDate toDate);
}

