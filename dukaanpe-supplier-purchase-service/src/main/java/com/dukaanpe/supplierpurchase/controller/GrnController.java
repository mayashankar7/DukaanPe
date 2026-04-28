package com.dukaanpe.supplierpurchase.controller;

import com.dukaanpe.supplierpurchase.dto.ApiResponse;
import com.dukaanpe.supplierpurchase.dto.GrnRequest;
import com.dukaanpe.supplierpurchase.dto.GrnResponse;
import com.dukaanpe.supplierpurchase.service.GrnService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grn")
@RequiredArgsConstructor
@Validated
public class GrnController {

    private final GrnService grnService;

    @PostMapping
    public ResponseEntity<ApiResponse<GrnResponse>> createGrn(@Valid @RequestBody GrnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("GRN created successfully", grnService.createGrn(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GrnResponse>>> listGrns(@RequestParam("storeId") Long storeId) {
        return ResponseEntity.ok(ApiResponse.success(grnService.listGrns(storeId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GrnResponse>> getGrn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(grnService.getGrn(id)));
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<GrnResponse>> verify(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("GRN verified", grnService.verifyGrn(id)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<GrnResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("GRN approved", grnService.approveGrn(id)));
    }
}

