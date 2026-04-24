package com.dukaanpe.store.service;

import com.dukaanpe.store.dto.LicenseInfoResponse;
import com.dukaanpe.store.dto.StoreRequest;
import com.dukaanpe.store.dto.StoreResponse;
import com.dukaanpe.store.dto.StoreStaffRequest;
import com.dukaanpe.store.dto.StoreStaffResponse;
import com.dukaanpe.store.dto.StoreTimingRequest;
import com.dukaanpe.store.dto.StoreTimingResponse;
import com.dukaanpe.store.dto.UpdateLicensesRequest;
import com.dukaanpe.store.dto.UpdateSubscriptionRequest;
import com.dukaanpe.store.entity.BusinessCategory;
import java.util.List;

public interface StoreService {

    StoreResponse createStore(StoreRequest request);

    List<StoreResponse> listStores(String ownerPhone);

    StoreResponse getStoreById(Long id);

    StoreResponse updateStore(Long id, StoreRequest request);

    void deactivateStore(Long id);

    List<StoreTimingResponse> getStoreTimings(Long id);

    List<StoreTimingResponse> updateStoreTimings(Long id, List<StoreTimingRequest> request);

    StoreStaffResponse addStaff(Long storeId, StoreStaffRequest request);

    List<StoreStaffResponse> listStaff(Long storeId);

    StoreStaffResponse updateStaff(Long storeId, Long staffId, StoreStaffRequest request);

    void removeStaff(Long storeId, Long staffId);

    List<BusinessCategory> listCategories();

    StoreResponse updateSubscription(Long storeId, UpdateSubscriptionRequest request);

    LicenseInfoResponse getLicenses(Long storeId);

    LicenseInfoResponse updateLicenses(Long storeId, UpdateLicensesRequest request);
}

