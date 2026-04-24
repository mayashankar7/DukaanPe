package com.dukaanpe.store.controller;

import com.dukaanpe.store.dto.ApiResponse;
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
import com.dukaanpe.store.service.StoreService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Validated
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody StoreRequest request) {
        StoreResponse response = storeService.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Store created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> listStores(@RequestParam(value = "ownerPhone", required = false) String ownerPhone) {
        return ResponseEntity.ok(ApiResponse.success(storeService.listStores(ownerPhone)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStoreById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
        @PathVariable("id") Long id,
        @Valid @RequestBody StoreRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store updated", storeService.updateStore(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateStore(@PathVariable("id") Long id) {
        storeService.deactivateStore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/timings")
    public ResponseEntity<ApiResponse<List<StoreTimingResponse>>> getStoreTimings(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStoreTimings(id)));
    }

    @PutMapping("/{id}/timings")
    public ResponseEntity<ApiResponse<List<StoreTimingResponse>>> updateStoreTimings(
        @PathVariable("id") Long id,
        @Valid @RequestBody List<StoreTimingRequest> request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Store timings updated", storeService.updateStoreTimings(id, request)));
    }

    @PostMapping("/{id}/staff")
    public ResponseEntity<ApiResponse<StoreStaffResponse>> addStaff(
        @PathVariable("id") Long id,
        @Valid @RequestBody StoreStaffRequest request
    ) {
        StoreStaffResponse response = storeService.addStaff(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Staff member added", response));
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<ApiResponse<List<StoreStaffResponse>>> listStaff(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(storeService.listStaff(id)));
    }

    @PutMapping("/{id}/staff/{staffId}")
    public ResponseEntity<ApiResponse<StoreStaffResponse>> updateStaff(
        @PathVariable("id") Long id,
        @PathVariable("staffId") Long staffId,
        @Valid @RequestBody StoreStaffRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Staff member updated", storeService.updateStaff(id, staffId, request)));
    }

    @DeleteMapping("/{id}/staff/{staffId}")
    public ResponseEntity<Void> removeStaff(@PathVariable("id") Long id, @PathVariable("staffId") Long staffId) {
        storeService.removeStaff(id, staffId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<BusinessCategory>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.success(storeService.listCategories()));
    }

    @PutMapping("/{id}/subscription")
    public ResponseEntity<ApiResponse<StoreResponse>> updateSubscription(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateSubscriptionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Subscription updated", storeService.updateSubscription(id, request)));
    }

    @GetMapping("/{id}/licenses")
    public ResponseEntity<ApiResponse<LicenseInfoResponse>> getLicenses(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getLicenses(id)));
    }

    @PutMapping("/{id}/licenses")
    public ResponseEntity<ApiResponse<LicenseInfoResponse>> updateLicenses(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateLicensesRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Licenses updated", storeService.updateLicenses(id, request)));
    }
}

