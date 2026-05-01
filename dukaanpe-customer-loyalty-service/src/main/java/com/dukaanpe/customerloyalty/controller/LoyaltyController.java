package com.dukaanpe.customerloyalty.controller;

import com.dukaanpe.customerloyalty.dto.ApiResponse;
import com.dukaanpe.customerloyalty.dto.CampaignResponse;
import com.dukaanpe.customerloyalty.dto.CustomerResponse;
import com.dukaanpe.customerloyalty.dto.CreateCampaignRequest;
import com.dukaanpe.customerloyalty.dto.EarnLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.dto.EligibleCustomerCountResponse;
import com.dukaanpe.customerloyalty.dto.LoyaltySettingsRequest;
import com.dukaanpe.customerloyalty.dto.LoyaltySettingsResponse;
import com.dukaanpe.customerloyalty.dto.LoyaltySummaryResponse;
import com.dukaanpe.customerloyalty.dto.LoyaltyTransactionResponse;
import com.dukaanpe.customerloyalty.dto.PagedResponse;
import com.dukaanpe.customerloyalty.dto.RedeemLoyaltyPointsRequest;
import com.dukaanpe.customerloyalty.dto.UpdateCampaignRequest;
import com.dukaanpe.customerloyalty.service.CampaignService;
import com.dukaanpe.customerloyalty.service.LoyaltyService;
import com.dukaanpe.customerloyalty.service.LoyaltySettingsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Validated
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final LoyaltySettingsService loyaltySettingsService;
    private final CampaignService campaignService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<LoyaltySummaryResponse>> getSummary(
        @PathVariable("customerId") @Positive(message = "customerId must be positive") Long customerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getLoyaltySummary(customerId)));
    }

    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> earnPoints(
        @Valid @RequestBody EarnLoyaltyPointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Points earned", loyaltyService.earnPoints(request)));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> redeemPoints(
        @Valid @RequestBody RedeemLoyaltyPointsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Points redeemed", loyaltyService.redeemPoints(request)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<LoyaltyTransactionResponse>>> getTransactions(
        @RequestParam("customerId") @Positive(message = "customerId must be positive") Long customerId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(loyaltyService.getTransactions(customerId, page, size)));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<LoyaltySettingsResponse>> getSettings(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(loyaltySettingsService.getSettings(storeId)));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<LoyaltySettingsResponse>> updateSettings(
        @Valid @RequestBody LoyaltySettingsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Loyalty settings updated", loyaltySettingsService.upsertSettings(request)));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
        @Valid @RequestBody CreateCampaignRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Campaign created", campaignService.createCampaign(request)));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<ApiResponse<PagedResponse<CampaignResponse>>> listCampaigns(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.listCampaigns(storeId, page, size)));
    }

    @GetMapping("/campaigns/active")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> listActiveCampaigns(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.listActiveCampaigns(storeId)));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.getCampaign(id)));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(
        @PathVariable("id") @Positive(message = "id must be positive") Long id,
        @Valid @RequestBody UpdateCampaignRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Campaign updated", campaignService.updateCampaign(id, request)));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deactivateCampaign(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        campaignService.deactivateCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campaigns/{id}/eligible-customers/preview")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> previewEligibleCustomers(
        @PathVariable("id") @Positive(message = "id must be positive") Long id,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.previewEligibleCustomers(id, page, size)));
    }

    @GetMapping("/campaigns/{id}/eligible-customers/preview/count")
    public ResponseEntity<ApiResponse<EligibleCustomerCountResponse>> countEligibleCustomers(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(campaignService.countEligibleCustomers(id)));
    }
}
