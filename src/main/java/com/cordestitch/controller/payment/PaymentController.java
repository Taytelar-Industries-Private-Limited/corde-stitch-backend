package com.cordestitch.controller.payment;

import com.razorpay.RazorpayException;
import com.cordestitch.request.payment.CardRequest;
import com.cordestitch.request.payment.PaymentRequest;
import com.cordestitch.request.payment.TransferToAccountRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.payment.CardDetailsResponse;
import com.cordestitch.response.payment.PaymentData;
import com.cordestitch.response.payment.PaymentResponse;
import com.cordestitch.service.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates a payment order using the provided payment request.

     * This API endpoint interacts with the Razorpay payment gateway to create a new payment order.
     * It takes a PaymentRequest object as input, which contains the necessary payment details.
     * The response includes the payment order details, such as the order ID and status.
     *
     * @param paymentRequest The request object containing the payment details.
     * @return A ResponseEntity containing the PaymentResponse with the order details and status.
     * @throws RazorpayException If there is an error while creating the payment order.
     */
    @PostMapping("/createPayment")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) throws RazorpayException {
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }


    /**
     * Verifies the payment signature returned by Razorpay.

     * This API endpoint verifies the Razorpay signature to ensure that the payment was not tampered with.
     * It takes a PaymentData object as input, which includes the payment details and signature.
     * The response indicates whether the signature verification was successful or not.
     *
     * @param paymentData The data object containing payment details and the Razorpay signature.
     * @return A ResponseEntity containing the SuccessResponse indicating the verification result.
     */
    @PostMapping("/verifySignature")
    public ResponseEntity<SuccessResponse> verifySignature(@Valid @RequestBody PaymentData paymentData) {
        SuccessResponse successResponse = paymentService.verifyRazorpaySignature(paymentData);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    /**
     * Creates a retry payment order using the provided payment request.

     * This API endpoint interacts with the Razorpay payment gateway to create a new payment order.
     * It takes a PaymentRequest object as input, which contains the necessary payment details.
     * The response includes the payment order details, such as the order ID and status.
     *
     * @param paymentRequest The request object containing the payment details.
     * @return A ResponseEntity containing the PaymentResponse with the order details and status.
     * @throws RazorpayException If there is an error while creating the payment order.
     */
    @PostMapping("/retryPayment")
    public ResponseEntity<PaymentResponse> retryPayment(@Valid @RequestBody PaymentRequest paymentRequest) throws RazorpayException {
        PaymentResponse paymentResponse = paymentService.retryPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.OK).body(paymentResponse);
    }

    /**
     * Adds a new card for a user.
     * This API endpoint allows a user to add a new card by providing the necessary card details
     * in a CardRequest object. The response indicates whether the card addition was successful.
     * @param cardRequest The request object containing the card details to be added.
     * @return A ResponseEntity containing the SuccessResponse indicating the result of the addition.
     */
    @PostMapping("/addCard")
    public ResponseEntity<SuccessResponse> addCard(@Valid @RequestBody CardRequest cardRequest) {
        SuccessResponse successResponse = paymentService.addCard(cardRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    /**
     * Retrieves all cards associated with a specific user.
     * This API endpoint retrieves a list of card details for the user specified by userId.
     * @param request The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing a list of CardDetailsResponse associated with the user.
     */
    @GetMapping("/getAllCards")
    public ResponseEntity<List<CardDetailsResponse>> getAllCards(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        List<CardDetailsResponse> cardResponse = paymentService.getAllCards(userId);
        return ResponseEntity.status(HttpStatus.OK).body(cardResponse);
    }

    /**
     * Deletes a specific card for a user.
     * This API endpoint deletes the card specified by cardId for the user specified by userId.
     * @param cardId The ID of the card to be deleted.
     * @param request The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing a SuccessResponse indicating the result of the deletion.
     */
    @DeleteMapping("/deleteCard")
    public ResponseEntity<SuccessResponse> deleteCard(@NotBlank @RequestParam String cardId, HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        SuccessResponse successResponse = paymentService.deleteCard(cardId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @GetMapping("/reconcilePayments")
    public ResponseEntity<SuccessResponse> reconcilePayments() {
        SuccessResponse successResponse = paymentService.reconcilePendingPayments();
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @GetMapping("/refund-status-checking")
    public ResponseEntity<List<SuccessResponse>> refundStatusChecking() {
        List<SuccessResponse> successResponse = paymentService.refundStatusChecking();
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @PostMapping("/transfer-to-account")
    public ResponseEntity<SuccessResponse> transferToAccount(@Valid @RequestBody TransferToAccountRequest request) {
        SuccessResponse successResponse = paymentService.transferToAccount(request);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @GetMapping("/initiate-refund")
    public ResponseEntity<List<SuccessResponse>> initiateRefund(){
        List<SuccessResponse> successResponse = paymentService.initiateRefund();
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

}
