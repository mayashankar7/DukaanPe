package com.dukaanpe.store.dto;

import com.dukaanpe.store.entity.BusinessCategory;
import com.dukaanpe.store.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "Owner phone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Owner phone must be a valid 10 digit Indian mobile")
    private String ownerPhone;

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must be at most 150 characters")
    private String storeName;

    @Size(max = 150, message = "Regional store name must be at most 150 characters")
    private String storeNameRegional;

    @NotNull(message = "Business category is required")
    private BusinessCategory businessCategory;

    @Size(max = 255, message = "Address line 1 must be at most 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be at most 255 characters")
    private String addressLine2;

    @Size(max = 80, message = "City must be at most 80 characters")
    private String city;

    @Size(max = 80, message = "State must be at most 80 characters")
    private String state;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone must be a valid 10 digit Indian mobile")
    private String phone;

    @Pattern(regexp = "^[0-9A-Z]{15}$", message = "GSTIN must be 15 alpha-numeric uppercase characters")
    private String gstin;

    private String fssaiLicense;
    private String drugLicense;
    private String tradeLicense;
    private Double latitude;
    private Double longitude;
    private String logoUrl;
    private SubscriptionPlan subscriptionPlan;
}

