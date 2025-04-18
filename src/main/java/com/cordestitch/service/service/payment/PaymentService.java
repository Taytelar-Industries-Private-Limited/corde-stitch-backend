package com.cordestitch.service.service.payment;

import com.razorpay.RazorpayException;
import com.cordestitch.request.payment.CardRequest;
import com.cordestitch.request.payment.PaymentRequest;
import com.cordestitch.request.payment.TransferToAccountRequest;
import com.cordestitch.request.user.UserBankDetailsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.payment.CardDetailsResponse;
import com.cordestitch.response.payment.PaymentData;
import com.cordestitch.response.payment.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest paymentRequest) throws RazorpayException;

    SuccessResponse verifyRazorpaySignature(PaymentData paymentData);

    SuccessResponse addCard(CardRequest cardRequest);

    List<CardDetailsResponse> getAllCards(String userId);

    SuccessResponse deleteCard(String cardId, String userId);

    PaymentResponse retryPayment(PaymentRequest paymentRequest);

    SuccessResponse reconcilePendingPayments();

    List<SuccessResponse> refundStatusChecking();

    SuccessResponse transferToAccount(TransferToAccountRequest request);

    String createRazorpayContact(String accountHolderName, String emailAddress, String phoneNumber);

    String createFundAccount(String razorpayContactId, UserBankDetailsRequest request);

    List<SuccessResponse> initiateRefund();
}
