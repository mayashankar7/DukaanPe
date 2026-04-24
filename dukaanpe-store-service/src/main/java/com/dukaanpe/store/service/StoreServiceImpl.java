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
import com.dukaanpe.store.entity.Store;
import com.dukaanpe.store.entity.StoreStaff;
import com.dukaanpe.store.entity.StoreTiming;
import com.dukaanpe.store.entity.SubscriptionPlan;
import com.dukaanpe.store.exception.ResourceNotFoundException;
import com.dukaanpe.store.repository.StoreRepository;
import com.dukaanpe.store.repository.StoreStaffRepository;
import com.dukaanpe.store.repository.StoreTimingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreTimingRepository storeTimingRepository;
    private final StoreStaffRepository storeStaffRepository;

    @Override
    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        Store store = mapToStore(new Store(), request);
        if (store.getSubscriptionPlan() == null) {
            store.setSubscriptionPlan(SubscriptionPlan.FREE);
        }
        return toStoreResponse(storeRepository.save(store));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreResponse> listStores(String ownerPhone) {
        if (ownerPhone == null || ownerPhone.isBlank()) {
            return storeRepository.findAll().stream().filter(Store::getIsActive).map(this::toStoreResponse).toList();
        }
        return storeRepository.findByOwnerPhoneAndIsActiveTrue(ownerPhone).stream().map(this::toStoreResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id) {
        return toStoreResponse(getActiveStore(id));
    }

    @Override
    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request) {
        Store store = getActiveStore(id);
        mapToStore(store, request);
        return toStoreResponse(storeRepository.save(store));
    }

    @Override
    @Transactional
    public void deactivateStore(Long id) {
        Store store = getActiveStore(id);
        store.setIsActive(false);
        storeRepository.save(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreTimingResponse> getStoreTimings(Long id) {
        Store store = getActiveStore(id);
        return storeTimingRepository.findByStoreOrderByDayOfWeekAsc(store).stream().map(this::toTimingResponse).toList();
    }

    @Override
    @Transactional
    public List<StoreTimingResponse> updateStoreTimings(Long id, List<StoreTimingRequest> requests) {
        Store store = getActiveStore(id);
        storeTimingRepository.deleteByStore(store);
        List<StoreTiming> timings = requests.stream().map(req -> StoreTiming.builder()
            .store(store)
            .dayOfWeek(req.getDayOfWeek())
            .openTime(req.getOpenTime())
            .closeTime(req.getCloseTime())
            .isClosed(req.getIsClosed() != null ? req.getIsClosed() : false)
            .build()).toList();
        return storeTimingRepository.saveAll(timings).stream().map(this::toTimingResponse).toList();
    }

    @Override
    @Transactional
    public StoreStaffResponse addStaff(Long storeId, StoreStaffRequest request) {
        Store store = getActiveStore(storeId);
        StoreStaff staff = StoreStaff.builder()
            .store(store)
            .staffPhone(request.getStaffPhone())
            .staffName(request.getStaffName())
            .role(request.getRole())
            .isActive(request.getIsActive() == null ? true : request.getIsActive())
            .build();
        return toStaffResponse(storeStaffRepository.save(staff));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreStaffResponse> listStaff(Long storeId) {
        Store store = getActiveStore(storeId);
        return storeStaffRepository.findByStoreAndIsActiveTrue(store).stream().map(this::toStaffResponse).toList();
    }

    @Override
    @Transactional
    public StoreStaffResponse updateStaff(Long storeId, Long staffId, StoreStaffRequest request) {
        Store store = getActiveStore(storeId);
        StoreStaff staff = storeStaffRepository.findById(staffId)
            .filter(existing -> existing.getStore().getId().equals(store.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        staff.setStaffPhone(request.getStaffPhone());
        staff.setStaffName(request.getStaffName());
        staff.setRole(request.getRole());
        if (request.getIsActive() != null) {
            staff.setIsActive(request.getIsActive());
        }
        return toStaffResponse(storeStaffRepository.save(staff));
    }

    @Override
    @Transactional
    public void removeStaff(Long storeId, Long staffId) {
        Store store = getActiveStore(storeId);
        StoreStaff staff = storeStaffRepository.findById(staffId)
            .filter(existing -> existing.getStore().getId().equals(store.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
        staff.setIsActive(false);
        storeStaffRepository.save(staff);
    }

    @Override
    public List<BusinessCategory> listCategories() {
        return List.of(BusinessCategory.values());
    }

    @Override
    @Transactional
    public StoreResponse updateSubscription(Long storeId, UpdateSubscriptionRequest request) {
        Store store = getActiveStore(storeId);
        store.setSubscriptionPlan(request.getSubscriptionPlan());
        return toStoreResponse(storeRepository.save(store));
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseInfoResponse getLicenses(Long storeId) {
        Store store = getActiveStore(storeId);
        return toLicenseResponse(store);
    }

    @Override
    @Transactional
    public LicenseInfoResponse updateLicenses(Long storeId, UpdateLicensesRequest request) {
        Store store = getActiveStore(storeId);
        store.setGstin(request.getGstin());
        store.setFssaiLicense(request.getFssaiLicense());
        store.setDrugLicense(request.getDrugLicense());
        store.setTradeLicense(request.getTradeLicense());
        return toLicenseResponse(storeRepository.save(store));
    }

    private Store getActiveStore(Long id) {
        Store store = storeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
        if (!Boolean.TRUE.equals(store.getIsActive())) {
            throw new ResourceNotFoundException("Store is inactive with id: " + id);
        }
        return store;
    }

    private Store mapToStore(Store store, StoreRequest request) {
        store.setOwnerPhone(request.getOwnerPhone());
        store.setStoreName(request.getStoreName());
        store.setStoreNameRegional(request.getStoreNameRegional());
        store.setBusinessCategory(request.getBusinessCategory());
        store.setAddressLine1(request.getAddressLine1());
        store.setAddressLine2(request.getAddressLine2());
        store.setCity(request.getCity());
        store.setState(request.getState());
        store.setPincode(request.getPincode());
        store.setPhone(request.getPhone());
        store.setGstin(request.getGstin());
        store.setFssaiLicense(request.getFssaiLicense());
        store.setDrugLicense(request.getDrugLicense());
        store.setTradeLicense(request.getTradeLicense());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setLogoUrl(request.getLogoUrl());
        if (request.getSubscriptionPlan() != null) {
            store.setSubscriptionPlan(request.getSubscriptionPlan());
        }
        return store;
    }

    private StoreResponse toStoreResponse(Store store) {
        return StoreResponse.builder()
            .id(store.getId())
            .ownerPhone(store.getOwnerPhone())
            .storeName(store.getStoreName())
            .storeNameRegional(store.getStoreNameRegional())
            .businessCategory(store.getBusinessCategory())
            .addressLine1(store.getAddressLine1())
            .addressLine2(store.getAddressLine2())
            .city(store.getCity())
            .state(store.getState())
            .pincode(store.getPincode())
            .phone(store.getPhone())
            .gstin(store.getGstin())
            .fssaiLicense(store.getFssaiLicense())
            .drugLicense(store.getDrugLicense())
            .tradeLicense(store.getTradeLicense())
            .latitude(store.getLatitude())
            .longitude(store.getLongitude())
            .logoUrl(store.getLogoUrl())
            .isActive(store.getIsActive())
            .subscriptionPlan(store.getSubscriptionPlan())
            .createdAt(store.getCreatedAt())
            .updatedAt(store.getUpdatedAt())
            .build();
    }

    private StoreTimingResponse toTimingResponse(StoreTiming timing) {
        return StoreTimingResponse.builder()
            .id(timing.getId())
            .dayOfWeek(timing.getDayOfWeek())
            .openTime(timing.getOpenTime())
            .closeTime(timing.getCloseTime())
            .isClosed(timing.getIsClosed())
            .build();
    }

    private StoreStaffResponse toStaffResponse(StoreStaff staff) {
        return StoreStaffResponse.builder()
            .id(staff.getId())
            .staffPhone(staff.getStaffPhone())
            .staffName(staff.getStaffName())
            .role(staff.getRole())
            .isActive(staff.getIsActive())
            .createdAt(staff.getCreatedAt())
            .build();
    }

    private LicenseInfoResponse toLicenseResponse(Store store) {
        return LicenseInfoResponse.builder()
            .gstin(store.getGstin())
            .fssaiLicense(store.getFssaiLicense())
            .drugLicense(store.getDrugLicense())
            .tradeLicense(store.getTradeLicense())
            .build();
    }
}

