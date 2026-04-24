package com.dukaanpe.store.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateLicensesRequest {

    @Pattern(regexp = "^[0-9A-Z]{15}$", message = "GSTIN must be 15 alpha-numeric uppercase characters")
    private String gstin;

    private String fssaiLicense;
    private String drugLicense;
    private String tradeLicense;
}

