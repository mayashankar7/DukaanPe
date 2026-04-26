package com.dukaanpe.billing.service;

import com.dukaanpe.billing.dto.BillResponse;
import com.dukaanpe.billing.dto.BillingSummaryResponse;
import com.dukaanpe.billing.dto.CalculateBillRequest;
import com.dukaanpe.billing.dto.CalculateBillResponse;
import com.dukaanpe.billing.dto.CreateBillRequest;
import com.dukaanpe.billing.dto.PagedResponse;
import java.time.LocalDate;

public interface BillingService {

    BillResponse createBill(CreateBillRequest request);

    PagedResponse<BillResponse> listBills(Long storeId, LocalDate date, int page, int size);

    BillResponse getBill(Long id);

    BillResponse getBillByNumber(String billNumber);

    BillResponse cancelBill(Long id);

    BillingSummaryResponse todaySummary(Long storeId);

    PagedResponse<BillResponse> searchBills(Long storeId, String query, int page, int size);

    CalculateBillResponse calculate(CalculateBillRequest request);
}

