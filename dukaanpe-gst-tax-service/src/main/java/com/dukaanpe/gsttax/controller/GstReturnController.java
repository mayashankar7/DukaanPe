package com.dukaanpe.gsttax.controller;

import com.dukaanpe.gsttax.dto.ApiResponse;
import com.dukaanpe.gsttax.dto.GstReturnResponse;
import com.dukaanpe.gsttax.dto.PrepareGstReturnRequest;
import com.dukaanpe.gsttax.service.GstReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gst/returns")
@RequiredArgsConstructor
@Validated
public class GstReturnController {

    private final GstReturnService gstReturnService;

    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<GstReturnResponse>> prepare(@Valid @RequestBody PrepareGstReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("GST return prepared", gstReturnService.prepare(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GstReturnResponse>> getById(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(gstReturnService.getById(id)));
    }
}

