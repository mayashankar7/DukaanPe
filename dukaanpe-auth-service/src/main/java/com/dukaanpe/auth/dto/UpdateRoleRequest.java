package com.dukaanpe.auth.dto;

import com.dukaanpe.auth.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private UserRole role;
}

