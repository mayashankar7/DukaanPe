package com.dukaanpe.customerloyalty.service;

import com.dukaanpe.customerloyalty.dto.CreateCustomerRequest;
import com.dukaanpe.customerloyalty.dto.CustomerResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.PurchaseHistoryResponse;
import com.dukaanpe.customerloyalty.dto.RecordPurchaseRequest;
import com.dukaanpe.customerloyalty.dto.UpdateCustomerRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    PagedResponse<CustomerResponse> listCustomers(Long storeId, int page, int size);

    PagedResponse<CustomerResponse> searchCustomers(
        Long storeId,
        String query,
        String tier,
        BigDecimal minPurchases,
        LocalDate lastVisitFrom,
        LocalDate lastVisitTo,
        int page,
        int size
    );

    CustomerResponse findByPhone(Long storeId, String phone);

    List<CustomerResponse> topCustomers(Long storeId, int limit);

    CustomerResponse getCustomer(Long id);

    CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);

    void deactivateCustomer(Long id);

    PurchaseHistoryResponse recordPurchase(Long customerId, RecordPurchaseRequest request);

    PagedResponse<PurchaseHistoryResponse> getPurchaseHistory(Long customerId, int page, int size);
}
