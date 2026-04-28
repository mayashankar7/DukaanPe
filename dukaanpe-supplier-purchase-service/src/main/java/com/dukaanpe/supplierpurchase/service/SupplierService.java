package com.dukaanpe.supplierpurchase.service;

import com.dukaanpe.supplierpurchase.dto.SupplierRequest;
import com.dukaanpe.supplierpurchase.dto.SupplierResponse;
import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest request);

    List<SupplierResponse> listSuppliers(Long storeId);

    SupplierResponse getSupplier(Long id);

    SupplierResponse updateSupplier(Long id, SupplierRequest request);

    void deactivateSupplier(Long id);

    List<SupplierResponse> searchSuppliers(Long storeId, String q);
}

