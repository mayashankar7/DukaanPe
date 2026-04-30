package com.dukaanpe.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateUpiQrResponse {

    private String qrCodeData;
}

