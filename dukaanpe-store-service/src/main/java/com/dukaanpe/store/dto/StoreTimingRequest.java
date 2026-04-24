package com.dukaanpe.store.dto;

import com.dukaanpe.store.entity.StoreDayOfWeek;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Data;

@Data
public class StoreTimingRequest {

    @NotNull(message = "Day of week is required")
    private StoreDayOfWeek dayOfWeek;

    private LocalTime openTime;

    private LocalTime closeTime;

    private Boolean isClosed;
}

