package com.cordestitch.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.payment.CardRequest;
import com.cordestitch.request.payment.PaymentRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.payment.CardDetailsResponse;
import com.cordestitch.response.payment.PaymentData;
import com.cordestitch.response.payment.PaymentResponse;
import com.cordestitch.service.service.payment.PaymentService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @MockBean
    public PaymentService paymentService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void createPayment() throws Exception {
        PaymentRequest paymentRequest = getPaymentPojo();
        when(paymentService.createPayment(any())).thenReturn(new PaymentResponse());
        mvc.perform(post("/api/payment/createPayment").content(objectMapper.writeValueAsString(paymentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void verifySignatureWhenSignatureIsValid() throws Exception {
        PaymentData paymentData = getPaymentData();
        when(paymentService.verifyRazorpaySignature(any())).thenReturn(new SuccessResponse());
        mvc.perform(post("/api/payment/verifySignature").content(objectMapper.writeValueAsString(paymentData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRetryPayment() throws Exception {
        PaymentRequest paymentRequest = getPaymentPojo();
        when(paymentService.retryPayment(any())).thenReturn(new PaymentResponse());
        mvc.perform(post("/api/payment/retryPayment")
                        .content(objectMapper.writeValueAsString(paymentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddCard() throws Exception {
        CardRequest cardRequest = getCardRequest();
        when(paymentService.addCard(any())).thenReturn(new SuccessResponse());
        mvc.perform(post("/api/payment/addCard")
                        .content(objectMapper.writeValueAsString(cardRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllCards() throws Exception {
        when(paymentService.getAllCards(any())).thenReturn(List.of(new CardDetailsResponse()));
        mvc.perform(get("/api/payment/getAllCards")
                        .param("userId", ENCRYPTED_USER_ID)
                        .content(objectMapper.writeValueAsString(ENCRYPTED_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCard() throws Exception {
        String cardId = "card1";
        when(paymentService.deleteCard(any(), any())).thenReturn(new SuccessResponse());
        mvc.perform(delete("/api/payment/deleteCard")
                        .param("cardId", cardId)
                        .param("userId", ENCRYPTED_USER_ID)
                        .content(objectMapper.writeValueAsString(cardId))
                        .content(objectMapper.writeValueAsString(ENCRYPTED_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testReconcilePayments() throws Exception{
        when(paymentService.reconcilePendingPayments()).thenReturn(new SuccessResponse());
        mvc.perform(get("/api/payment/reconcilePayments")
         .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRefundStatusChecking() throws Exception{
        when(paymentService.refundStatusChecking()).thenReturn(List.of(new SuccessResponse()));
        mvc.perform(get("/api/payment/refund-status-checking")
         .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private CardRequest getCardRequest() {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setUserId(ENCRYPTED_USER_ID);
        cardRequest.setCardType("Rupay");
        cardRequest.setCardHolderName("jay");
        cardRequest.setToken("token1");
        cardRequest.setExpirationDate("05/29");
        cardRequest.setLastFourDigits("1234");
        return cardRequest;
    }


    private PaymentRequest getPaymentPojo() {
        PaymentRequest paymentDto = new PaymentRequest();
        paymentDto.setUserId(ENCRYPTED_USER_ID);
        paymentDto.setOrderId("ORDER_123");
        paymentDto.setTotalAmount(400.00);
        paymentDto.setPaymentMethod(Constants.COD);
        return paymentDto;
    }

    private PaymentData getPaymentData() {
        PaymentData paymentData = new PaymentData();
        paymentData.setOrderId("order123");
        paymentData.setRazorPayOrderId("abc");
        paymentData.setRazorPayPaymentId("abd");
        paymentData.setRazorPaySignature("xyz");
        return paymentData;
    }
}