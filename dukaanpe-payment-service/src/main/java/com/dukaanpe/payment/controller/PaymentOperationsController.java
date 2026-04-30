package com.dukaanpe.payment.controller;

import com.dukaanpe.payment.dto.ApiResponse;
import com.dukaanpe.payment.dto.CashRegisterResponse;
import com.dukaanpe.payment.dto.CloseCashRegisterRequest;
import com.dukaanpe.payment.dto.CreateReconciliationRequest;
import com.dukaanpe.payment.dto.GenerateUpiLinkRequest;
import com.dukaanpe.payment.dto.GenerateUpiLinkResponse;
import com.dukaanpe.payment.dto.GenerateUpiQrResponse;
import com.dukaanpe.payment.dto.OpenCashRegisterRequest;
import com.dukaanpe.payment.dto.PaymentReconciliationResponse;
import com.dukaanpe.payment.dto.SaveUpiQrCodeRequest;
import com.dukaanpe.payment.dto.UpiQrCodeResponse;
import com.dukaanpe.payment.service.PaymentOperationsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentOperationsController {

    private final PaymentOperationsService paymentOperationsService;

    @PostMapping("/upi/generate-link")
    public ResponseEntity<ApiResponse<GenerateUpiLinkResponse>> generateUpiLink(
        @Valid @RequestBody GenerateUpiLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentOperationsService.generateUpiLink(request)));
    }

    @PostMapping("/upi/generate-qr")
    public ResponseEntity<ApiResponse<GenerateUpiQrResponse>> generateUpiQr(
        @Valid @RequestBody GenerateUpiLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentOperationsService.generateUpiQr(request)));
    }

    @GetMapping("/upi/qr-codes")
    public ResponseEntity<ApiResponse<List<UpiQrCodeResponse>>> getSavedQrCodes(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentOperationsService.getUpiQrCodes(storeId)));
    }

    @PostMapping("/upi/qr-codes")
    public ResponseEntity<ApiResponse<UpiQrCodeResponse>> saveQrCode(
        @Valid @RequestBody SaveUpiQrCodeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("QR code saved", paymentOperationsService.saveUpiQrCode(request)));
    }

    @PostMapping("/reconciliation")
    public ResponseEntity<ApiResponse<PaymentReconciliationResponse>> createReconciliation(
        @Valid @RequestBody CreateReconciliationRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Reconciliation created", paymentOperationsService.createReconciliation(request, idempotencyKey)));
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<ApiResponse<List<PaymentReconciliationResponse>>> monthlyReconciliation(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("month") @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$", message = "month must be YYYY-MM") String month
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentOperationsService.getMonthlyReconciliation(storeId, month)));
    }

    @PostMapping("/cash-register/open")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> openCashRegister(
        @Valid @RequestBody OpenCashRegisterRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Cash register opened", paymentOperationsService.openCashRegister(request, idempotencyKey)));
    }

    @PostMapping("/cash-register/close")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> closeCashRegister(
        @Valid @RequestBody CloseCashRegisterRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cash register closed", paymentOperationsService.closeCashRegister(request, idempotencyKey)));
    }

    @GetMapping("/cash-register/current")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> currentCashRegister(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "terminalId", defaultValue = "DEFAULT") String terminalId
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentOperationsService.getCurrentCashRegister(storeId, terminalId)));
    }
}

