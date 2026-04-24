package com.dukaanpe.store.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_phone", nullable = false, length = 10)
    private String ownerPhone;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_name_regional")
    private String storeNameRegional;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_category", nullable = false)
    private BusinessCategory businessCategory;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    private String city;

    private String state;

    @Column(length = 6)
    private String pincode;

    @Column(length = 10)
    private String phone;

    @Column(length = 15)
    private String gstin;

    @Column(name = "fssai_license")
    private String fssaiLicense;

    @Column(name = "drug_license")
    private String drugLicense;

    @Column(name = "trade_license")
    private String tradeLicense;

    private Double latitude;

    private Double longitude;

    @Column(name = "logo_url")
    private String logoUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StoreTiming> timings = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StoreStaff> staff = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.subscriptionPlan == null) {
            this.subscriptionPlan = SubscriptionPlan.FREE;
        }
        if (this.businessCategory == null) {
            this.businessCategory = BusinessCategory.OTHER;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

