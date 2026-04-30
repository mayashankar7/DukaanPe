package com.dukaanpe.payment.service;

import com.dukaanpe.payment.dto.CompletePaymentRequest;
import com.dukaanpe.payment.dto.FailPaymentRequest;
import com.dukaanpe.payment.dto.InitiatePaymentRequest;
import com.dukaanpe.payment.dto.PagedResponse;
import com.dukaanpe.payment.dto.PaymentTransactionResponse;
import com.dukaanpe.payment.dto.RefundPaymentRequest;
import java.time.LocalDate;

public interface PaymentTransactionService {

    PaymentTransactionResponse initiatePayment(InitiatePaymentRequest request, String idempotencyKey);

    PaymentTransactionResponse completePayment(String transactionId, CompletePaymentRequest request);

    PaymentTransactionResponse failPayment(String transactionId, FailPaymentRequest request);

    PaymentTransactionResponse getByTransactionId(String transactionId);

    PagedResponse<PaymentTransactionResponse> listTransactions(Long storeId, LocalDate date, int page, int size);

    PaymentTransactionResponse refundPayment(String transactionId, RefundPaymentRequest request);
}

