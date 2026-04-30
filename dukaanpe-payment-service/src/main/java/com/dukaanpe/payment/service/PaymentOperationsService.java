package com.dukaanpe.payment.service;

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
import java.util.List;

public interface PaymentOperationsService {

    GenerateUpiLinkResponse generateUpiLink(GenerateUpiLinkRequest request);

    GenerateUpiQrResponse generateUpiQr(GenerateUpiLinkRequest request);

    UpiQrCodeResponse saveUpiQrCode(SaveUpiQrCodeRequest request);

    List<UpiQrCodeResponse> getUpiQrCodes(Long storeId);

    PaymentReconciliationResponse createReconciliation(CreateReconciliationRequest request, String idempotencyKey);

    List<PaymentReconciliationResponse> getMonthlyReconciliation(Long storeId, String month);

    CashRegisterResponse openCashRegister(OpenCashRegisterRequest request, String idempotencyKey);

    CashRegisterResponse closeCashRegister(CloseCashRegisterRequest request, String idempotencyKey);

    CashRegisterResponse getCurrentCashRegister(Long storeId, String terminalId);
}

