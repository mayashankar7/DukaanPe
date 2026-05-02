package com.dukaanpe.gsttax.controller;

import com.dukaanpe.gsttax.dto.ApiResponse;
import com.dukaanpe.gsttax.dto.CreateHsnRequest;
import com.dukaanpe.gsttax.dto.HsnResponse;
import com.dukaanpe.gsttax.dto.PagedResponse;
import com.dukaanpe.gsttax.dto.UpdateHsnRequest;
import com.dukaanpe.gsttax.service.HsnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gst/hsn")
@RequiredArgsConstructor
@Validated
public class HsnController {

    private final HsnService hsnService;

    @PostMapping
    public ResponseEntity<ApiResponse<HsnResponse>> create(@Valid @RequestBody CreateHsnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("HSN created", hsnService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HsnResponse>> getById(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(hsnService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<HsnResponse>>> list(
        @RequestParam(value = "hsnCode", required = false) String hsnCode,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(hsnService.list(hsnCode, description, page, size)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HsnResponse>> update(
        @PathVariable("id") @Positive(message = "id must be positive") Long id,
        @Valid @RequestBody UpdateHsnRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("HSN updated", hsnService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        hsnService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

