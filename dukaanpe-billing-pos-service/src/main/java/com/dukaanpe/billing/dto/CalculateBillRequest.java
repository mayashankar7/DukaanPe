package com.dukaanpe.billing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class CalculateBillRequest {

    @NotEmpty(message = "At least one bill item is required")
    @Valid
    private List<BillItemRequest> items;
}

