package com.dukaanpe.payment.controller;

import com.dukaanpe.payment.dto.ApiResponse;
import com.dukaanpe.payment.dto.CompletePaymentRequest;
import com.dukaanpe.payment.dto.FailPaymentRequest;
import com.dukaanpe.payment.dto.InitiatePaymentRequest;
import com.dukaanpe.payment.dto.PagedResponse;
import com.dukaanpe.payment.dto.PaymentTransactionResponse;
import com.dukaanpe.payment.dto.RefundPaymentRequest;
import com.dukaanpe.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentTransactionService paymentTransactionService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> initiate(
        @Valid @RequestBody InitiatePaymentRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment initiated", paymentTransactionService.initiatePayment(request, idempotencyKey)));
    }

    @PutMapping("/{transactionId}/complete")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> complete(
        @PathVariable("transactionId") @Pattern(regexp = "^TXN-[A-Za-z0-9]+$", message = "Invalid transactionId")
        String transactionId,
        @Valid @RequestBody(required = false) CompletePaymentRequest request
    ) {
        CompletePaymentRequest safeRequest = request == null ? new CompletePaymentRequest() : request;
        return ResponseEntity.ok(ApiResponse.success("Payment completed",
            paymentTransactionService.completePayment(transactionId, safeRequest)));
    }

    @PutMapping("/{transactionId}/fail")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> fail(
        @PathVariable("transactionId") @Pattern(regexp = "^TXN-[A-Za-z0-9]+$", message = "Invalid transactionId")
        String transactionId,
        @Valid @RequestBody FailPaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Payment marked failed",
            paymentTransactionService.failPayment(transactionId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PaymentTransactionResponse>>> list(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentTransactionService.listTransactions(storeId, date, page, size)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> get(
        @PathVariable("transactionId") @Pattern(regexp = "^TXN-[A-Za-z0-9]+$", message = "Invalid transactionId")
        String transactionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(paymentTransactionService.getByTransactionId(transactionId)));
    }

    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> refund(
        @PathVariable("transactionId") @Pattern(regexp = "^TXN-[A-Za-z0-9]+$", message = "Invalid transactionId")
        String transactionId,
        @Valid @RequestBody RefundPaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Payment refunded",
            paymentTransactionService.refundPayment(transactionId, request)));
    }
}

