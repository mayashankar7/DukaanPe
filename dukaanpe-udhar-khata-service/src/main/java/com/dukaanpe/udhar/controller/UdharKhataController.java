package com.dukaanpe.udhar.controller;

import com.dukaanpe.udhar.dto.ApiResponse;
import com.dukaanpe.udhar.dto.CreateKhataCustomerRequest;
import com.dukaanpe.udhar.dto.CreateReminderRequest;
import com.dukaanpe.udhar.dto.CreateSettlementRequest;
import com.dukaanpe.udhar.dto.CreditRequest;
import com.dukaanpe.udhar.dto.KhataCustomerResponse;
import com.dukaanpe.udhar.dto.PagedResponse;
import com.dukaanpe.udhar.dto.PaymentRequest;
import com.dukaanpe.udhar.dto.ReminderResponse;
import com.dukaanpe.udhar.dto.SettlementMonthlyReportResponse;
import com.dukaanpe.udhar.dto.SettlementResponse;
import com.dukaanpe.udhar.dto.UdharEntryResponse;
import com.dukaanpe.udhar.dto.UdharSummaryResponse;
import com.dukaanpe.udhar.dto.UpdateKhataCustomerRequest;
import com.dukaanpe.udhar.service.UdharKhataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequiredArgsConstructor
@Validated
public class UdharKhataController {
    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be on or before toDate");
        }
    }


    private final UdharKhataService udharKhataService;

    @PostMapping("/api/khata/customers")
    public ResponseEntity<ApiResponse<KhataCustomerResponse>> addCustomer(@Valid @RequestBody CreateKhataCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Khata customer created", udharKhataService.addCustomer(request)));
    }

    @GetMapping("/api/khata/customers")
    public ResponseEntity<ApiResponse<PagedResponse<KhataCustomerResponse>>> listCustomers(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.listCustomers(storeId, page, size)));
    }

    @GetMapping("/api/khata/customers/{id}")
    public ResponseEntity<ApiResponse<KhataCustomerResponse>> getCustomer(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.getCustomer(id)));
    }

    @PutMapping("/api/khata/customers/{id}")
    public ResponseEntity<ApiResponse<KhataCustomerResponse>> updateCustomer(
        @PathVariable("id") @Positive(message = "id must be positive") Long id,
        @Valid @RequestBody UpdateKhataCustomerRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Khata customer updated", udharKhataService.updateCustomer(id, request)));
    }

    @DeleteMapping("/api/khata/customers/{id}")
    public ResponseEntity<Void> deactivateCustomer(@PathVariable("id") @Positive(message = "id must be positive") Long id) {
        udharKhataService.deactivateCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/khata/customers/search")
    public ResponseEntity<ApiResponse<PagedResponse<KhataCustomerResponse>>> searchCustomers(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("q") String query,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.searchCustomers(storeId, query, page, size)));
    }

    @GetMapping("/api/khata/customers/top-defaulters")
    public ResponseEntity<ApiResponse<List<KhataCustomerResponse>>> topDefaulters(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.topDefaulters(storeId)));
    }

    @PostMapping("/api/udhar/credit")
    public ResponseEntity<ApiResponse<UdharEntryResponse>> giveCredit(@Valid @RequestBody CreditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Credit entry created", udharKhataService.giveCredit(request)));
    }

    @PostMapping("/api/udhar/payment")
    public ResponseEntity<ApiResponse<UdharEntryResponse>> recordPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment entry created", udharKhataService.recordPayment(request)));
    }

    @GetMapping("/api/udhar/entries")
    public ResponseEntity<ApiResponse<PagedResponse<UdharEntryResponse>>> listEntries(
        @RequestParam("customerId") @Positive(message = "customerId must be positive") Long customerId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        validateDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.listEntries(customerId, fromDate, toDate, page, size)));
    }

    @GetMapping("/api/udhar/summary")
    public ResponseEntity<ApiResponse<UdharSummaryResponse>> summary(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.summary(storeId)));
    }

    @GetMapping("/api/udhar/overdue")
    public ResponseEntity<ApiResponse<PagedResponse<UdharEntryResponse>>> overdue(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        validateDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.overdue(storeId, fromDate, toDate, page, size)));
    }

    @PostMapping("/api/udhar/reminders")
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(@Valid @RequestBody CreateReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Reminder created", udharKhataService.createReminder(request)));
    }

    @GetMapping("/api/udhar/reminders/pending")
    public ResponseEntity<ApiResponse<PagedResponse<ReminderResponse>>> pendingReminders(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        validateDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.pendingReminders(storeId, fromDate, toDate, page, size)));
    }

    @PutMapping("/api/udhar/reminders/{id}/send")
    public ResponseEntity<ApiResponse<ReminderResponse>> markReminderSent(
        @PathVariable("id") @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Reminder marked as sent", udharKhataService.markReminderSent(id)));
    }

    @GetMapping("/api/udhar/reminders/history")
    public ResponseEntity<ApiResponse<PagedResponse<ReminderResponse>>> reminderHistory(
        @RequestParam("customerId") @Positive(message = "customerId must be positive") Long customerId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        validateDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.reminderHistory(customerId, fromDate, toDate, page, size)));
    }

    @PostMapping("/api/udhar/settlements")
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(@Valid @RequestBody CreateSettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Settlement created", udharKhataService.createSettlement(request)));
    }

    @GetMapping("/api/udhar/settlements")
    public ResponseEntity<ApiResponse<PagedResponse<SettlementResponse>>> listSettlements(
        @RequestParam("customerId") @Positive(message = "customerId must be positive") Long customerId,
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        validateDateRange(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.listSettlements(customerId, fromDate, toDate, page, size)));
    }

    @GetMapping("/api/udhar/settlements/report")
    public ResponseEntity<ApiResponse<SettlementMonthlyReportResponse>> settlementReport(
        @RequestParam("storeId") @Positive(message = "storeId must be positive") Long storeId,
        @RequestParam("month") String month,
        @RequestParam(value = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
        @RequestParam(value = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
        @Max(value = 100, message = "size must be <= 100") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(udharKhataService.settlementReport(storeId, month, page, size)));
    }
}

