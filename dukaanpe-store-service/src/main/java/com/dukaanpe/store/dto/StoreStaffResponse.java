package com.dukaanpe.store.dto;

import com.dukaanpe.store.entity.StoreStaffRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreStaffResponse {

    private Long id;
    private String staffPhone;
    private String staffName;
    private StoreStaffRole role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

