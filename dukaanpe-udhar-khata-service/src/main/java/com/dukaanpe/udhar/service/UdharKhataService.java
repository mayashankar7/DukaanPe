package com.dukaanpe.udhar.service;

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
import java.time.LocalDate;
import java.util.List;

public interface UdharKhataService {

    KhataCustomerResponse addCustomer(CreateKhataCustomerRequest request);

    PagedResponse<KhataCustomerResponse> listCustomers(Long storeId, int page, int size);

    KhataCustomerResponse getCustomer(Long id);

    KhataCustomerResponse updateCustomer(Long id, UpdateKhataCustomerRequest request);

    void deactivateCustomer(Long id);

    PagedResponse<KhataCustomerResponse> searchCustomers(Long storeId, String query, int page, int size);

    List<KhataCustomerResponse> topDefaulters(Long storeId);

    UdharEntryResponse giveCredit(CreditRequest request);

    UdharEntryResponse recordPayment(PaymentRequest request);

    PagedResponse<UdharEntryResponse> listEntries(Long customerId, LocalDate fromDate, LocalDate toDate, int page, int size);

    UdharSummaryResponse summary(Long storeId);

    PagedResponse<UdharEntryResponse> overdue(Long storeId, LocalDate fromDate, LocalDate toDate, int page, int size);

    ReminderResponse createReminder(CreateReminderRequest request);

    PagedResponse<ReminderResponse> pendingReminders(Long storeId, LocalDate fromDate, LocalDate toDate, int page, int size);

    ReminderResponse markReminderSent(Long reminderId);

    PagedResponse<ReminderResponse> reminderHistory(Long customerId, LocalDate fromDate, LocalDate toDate, int page, int size);

    SettlementResponse createSettlement(CreateSettlementRequest request);

    PagedResponse<SettlementResponse> listSettlements(Long customerId, LocalDate fromDate, LocalDate toDate, int page, int size);

    SettlementMonthlyReportResponse settlementReport(Long storeId, String month, int page, int size);
}

