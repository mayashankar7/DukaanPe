package com.dukaanpe.store.dto;

import com.dukaanpe.store.entity.StoreStaffRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreStaffRequest {

    @NotBlank(message = "Staff phone is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Staff phone must be a valid 10 digit Indian mobile")
    private String staffPhone;

    @NotBlank(message = "Staff name is required")
    @Size(max = 100, message = "Staff name must be at most 100 characters")
    private String staffName;

    @NotNull(message = "Role is required")
    private StoreStaffRole role;

    private Boolean isActive;
}

