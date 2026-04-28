package com.dukaanpe.supplierpurchase.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierResponse {

    private Long id;
    private Long storeId;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String alternatePhone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstin;
    private String categoriesSupplied;
    private String paymentTerms;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
    private Integer rating;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

