package com.dukaanpe.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseInfoResponse {

    private String gstin;
    private String fssaiLicense;
    private String drugLicense;
    private String tradeLicense;
}

