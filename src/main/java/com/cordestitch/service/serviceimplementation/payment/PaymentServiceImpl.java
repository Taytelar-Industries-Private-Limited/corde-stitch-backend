package com.cordestitch.service.serviceimplementation.payment;

import com.razorpay.*;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.exception.payment.*;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.payment.CardRepository;
import com.cordestitch.repository.payment.PaymentRepository;
import com.cordestitch.repository.payment.RefundRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.payment.CardRequest;
import com.cordestitch.request.payment.PaymentRequest;
import com.cordestitch.request.payment.TransferToAccountRequest;
import com.cordestitch.request.user.UserBankDetailsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.payment.CardDetailsResponse;
import com.cordestitch.response.payment.PaymentData;
import com.cordestitch.response.payment.PaymentResponse;
import com.cordestitch.response.payment.RefundResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.service.service.payment.PaymentService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.util.Generator;
import com.cordestitch.util.PaymentConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import com.cordestitch.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final CardRepository cardRepository;

    private final RefundRepository refundRepository;

    private final Generator generator;

    private final PaymentServiceHelper paymentServiceHelper;

    private final OrderItemRepository orderItemRepository;

    private final LoyaltyPointsService loyaltyPointsService;

    private final WhatsAppService whatsAppService;

    @Value("${razorpay.api.key.id}")
    private String keyID;

    @Value("${razorpay.api.key.secret}")
    private String keySecret;

    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");


    @Transactional
    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        log.info("Create Payment Request: {}", paymentRequest);

        validateUser(paymentRequest.getUserId());
        OrderEntity orderEntity = validateOrder(paymentRequest.getOrderId());
        Double orderTotalAmount = paymentRequest.getTotalAmount() + (isNull(orderEntity.getRedeemedLoyaltyPoints()) ? 0.0 : orderEntity.getRedeemedLoyaltyPoints());

        try {
            RazorpayClient razorPayClient = new RazorpayClient(keyID, keySecret);
            Order razorpayOrder = createRazorpayOrder(razorPayClient, paymentRequest.getTotalAmount(), "payment_receipt_");
            log.info("Created Razorpay order for the payment : {}", razorpayOrder);

            String razorpayOrderId = razorpayOrder.get("id");
            log.info("RazorPay Order ID : {}", razorpayOrderId);

            PaymentEntity paymentEntity = new PaymentEntity();
            paymentEntity.setPaymentId(generator.generateId(Constants.PAYMENT_ID));
            paymentEntity.setRazorPayOrderId(razorpayOrderId);
            paymentEntity.setTotalAmount(orderTotalAmount);
            paymentEntity.setPaymentDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
            paymentEntity.setUserId(paymentRequest.getUserId());
            paymentEntity.setPaymentMethod(paymentRequest.getPaymentMethod());
            paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
            paymentEntity.setOrderSubTotal(paymentRequest.getTotalAmount());
            paymentEntity.setOrderEntity(orderEntity);
            paymentRepository.save(paymentEntity);

            log.info("Payment Entity Saved: {}", paymentEntity);

            return buildPaymentResponse(Constants.PAYMENT_CREATED_SUCCESSFULLY, razorpayOrderId, paymentEntity.getPaymentDate());
        } catch (RazorpayException e) {
            log.error("While initiating the payment RazorPayException occurs: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create payment due to Razorpay exception: " + e.getMessage());
        }
    }


    public String generateRazorpaySignature(String razorPayOrderId, String razorPayPaymentId, String keySecret) {
        log.info("RazorPayOrderId: {} , RazorPayPaymentId : {}, KeySecret : {}", razorPayOrderId, razorPayPaymentId, keySecret);
        String signature = null;
        try {
            String signatureData = razorPayOrderId + "|" + razorPayPaymentId;
            Mac sha256HMAC = Mac.getInstance(PaymentConstants.ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keySecret.getBytes(), PaymentConstants.ALGORITHM);
            sha256HMAC.init(secretKeySpec);

            byte[] bytes = sha256HMAC.doFinal(signatureData.getBytes());

            StringBuilder builder = new StringBuilder();
            for (byte aByte : bytes) {
                builder.append(String.format("%02x", aByte));
            }

            signature = builder.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.debug("Exception while generating signature " + e.getMessage());
        }
        log.info("Generating signature : {}", signature);
        return signature;
    }

    @Override
    public SuccessResponse verifyRazorpaySignature(PaymentData paymentData) {
        log.info("Verify Razorpay signature Request : {}", paymentData);
        String generatedSignature = generateRazorpaySignature(paymentData.getRazorPayOrderId(), paymentData.getRazorPayPaymentId(), keySecret);
        PaymentEntity paymentEntity = paymentRepository.findByRazorPayOrderId(paymentData.getRazorPayOrderId());
        log.info("PaymentEntity : {}", paymentEntity);
        OrderEntity orderEntity = orderRepository.findByOrderId(paymentData.getOrderId());
        log.info("OrderEntity : {}", orderEntity);

        SuccessResponse successResponse = new SuccessResponse();
        if (generatedSignature != null && generatedSignature.equals(paymentData.getRazorPaySignature())) {

            orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
            for (OrderItemEntity orderItemEntity : orderEntity.getOrderItemEntities()) {
                orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
                orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
            }
            orderRepository.save(orderEntity);

            paymentEntity.setRazorPayPaymentId(paymentData.getRazorPayPaymentId());
            paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentEntity.setOrderEntity(orderEntity);
            paymentRepository.save(paymentEntity);
            log.info("Payment Successful, After payment entity saved : {}", paymentEntity);

            successResponse.setMessage(Constants.PAYMENT_SUCCESS);
            successResponse.setStatusCode(HttpStatus.OK.value());

            sendOrderConfirmation(orderEntity.getUserEntity(), orderEntity);
            log.info("Whatsapp notification sent successfully for order confirmation");

        } else {
            paymentEntity.setRazorPayPaymentId(paymentData.getRazorPayPaymentId());
            paymentEntity.setPaymentStatus(PaymentStatus.FAILED);
            paymentEntity.setOrderEntity(orderEntity);
            paymentRepository.save(paymentEntity);
            log.info("Payment failed, After payment entity saved : {}", paymentEntity);

            successResponse.setMessage(Constants.PAYMENT_FAILED);
            successResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        }
        log.info("Verify RazorPay Signature Response : {}", successResponse);
        return successResponse;
    }

    @Transactional
    @Override
    public PaymentResponse retryPayment(PaymentRequest paymentRequest) {
        log.info("Retry Payment Request : {}", paymentRequest);

        validateUser(paymentRequest.getUserId());
        OrderEntity orderEntity = validateOrder(paymentRequest.getOrderId());

        Double orderTotalAmount = paymentRequest.getTotalAmount() + (isNull(orderEntity.getRedeemedLoyaltyPoints()) ? 0.0 : orderEntity.getRedeemedLoyaltyPoints());

        PaymentEntity existingPayment = validateExistingPayment(paymentRequest.getOrderId());
        log.info("Existing Payment Data :{} ", existingPayment);

        if (!PaymentStatus.FAILED.equals(existingPayment.getPaymentStatus()) && !PaymentStatus.PENDING.equals(existingPayment.getPaymentStatus())) {
            throw new PaymentAlreadyConfirmedException(Constants.PAYMENT_ALREADY_CONFIRMED);
        }
        boolean isAmount = paymentRequest.getTotalAmount().equals(existingPayment.getOrderSubTotal());
        if(isAmount) {
            try {
                RazorpayClient razorPayClient = new RazorpayClient(keyID, keySecret);
                Order razorpayOrder = createRazorpayOrder(razorPayClient, existingPayment.getOrderSubTotal(), "retry_payment_receipt_");
                log.info("Created Razorpay order for the retry payment : {}", razorpayOrder);

                String newRazorpayOrderId = razorpayOrder.get("id");
                log.info("RazorPay Order ID : {}", newRazorpayOrderId);

                existingPayment.setRazorPayOrderId(newRazorpayOrderId);
                existingPayment.setTotalAmount(orderTotalAmount);
                existingPayment.setOrderSubTotal(paymentRequest.getTotalAmount());
                existingPayment.setPaymentStatus(PaymentStatus.PENDING);
                existingPayment.setPaymentDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
                paymentRepository.save(existingPayment);

                log.info("Updated Payment Entity for Retry payment: {}", existingPayment);

                return buildPaymentResponse(Constants.PAYMENT_RETRY_INITIATED_SUCCESSFULLY, newRazorpayOrderId, existingPayment.getPaymentDate());
            } catch (RazorpayException e) {
                log.error("While initiating the payment RazorPayException occurs: {}", e.getMessage());
                throw new PaymentProcessingException("Failed to create retry payment due to Razorpay exception: " + e.getMessage());
            }
        } else {
            log.error("Amount mismatch while retrying payment id {} and order id: {}", existingPayment.getRazorPayPaymentId(), existingPayment.getOrderEntity().getOrderId());
            throw new PaymentProcessingException(Constants.RETRY_PAYMENT_AMOUNT_MISMATCH);
        }
    }

    @Override
    public SuccessResponse addCard(CardRequest cardRequest) {
        log.info("Add Card Request : {}", cardRequest);
        UserEntity userEntity = userRepository.findUserByUserId(cardRequest.getUserId());
        if (isNull(userEntity)) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        CardEntity cardEntity = new CardEntity();
        cardEntity.setCardId(generator.generateId(Constants.CARD_ID));
        cardEntity.setUserEntity(userEntity);
        cardEntity.setCardType(cardRequest.getCardType());
        cardEntity.setRazorPayToken(cardRequest.getToken());
        cardEntity.setExpirationDate(cardRequest.getExpirationDate());
        cardEntity.setCardHolderName(cardRequest.getCardHolderName());
        cardEntity.setCardLastFourDigits(cardRequest.getLastFourDigits());
        cardRepository.save(cardEntity);
        log.info("Card Saved Data : {}", cardEntity);
        return new SuccessResponse(Constants.CARD_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    public List<CardDetailsResponse> getAllCards(String userId) {
        log.info("Get All Cards Request user Id : {}", userId);
        List<CardEntity> cardEntities = cardRepository.findByUserEntityUserId(userId);
        log.info("List of Cards {}", cardEntities);
        return cardEntities.stream()
                .map(cardEntity -> {
                    CardDetailsResponse cardDetailsResponse = new CardDetailsResponse();
                    cardDetailsResponse.setCardId(cardEntity.getCardId());
                    cardDetailsResponse.setUserId(cardEntity.getUserEntity().getUserId());
                    cardDetailsResponse.setCardType(cardEntity.getCardType());
                    cardDetailsResponse.setLastFourDigits(cardEntity.getCardLastFourDigits());
                    cardDetailsResponse.setCardHolderName(cardEntity.getCardHolderName());
                    cardDetailsResponse.setExpirationDate(cardEntity.getExpirationDate());
                    cardDetailsResponse.setToken(cardEntity.getRazorPayToken());
                    return cardDetailsResponse;
                })
                .toList();
    }

    @Override
    public SuccessResponse deleteCard(String cardId, String userId) {
        log.info("Delete the Card Request CardId {} UserId {}", cardId, userId);
        Optional<CardEntity> cardEntity = cardRepository.findByCardIdAndUserEntityUserId(cardId, userId);
        if (cardEntity.isEmpty()) {
            throw new CardNotFoundException(Constants.CARD_NOT_FOUND);
        }
        log.info("Card Entity for deletion : {}", cardEntity);
        cardEntity.ifPresent(cardRepository::delete);
        return new SuccessResponse(Constants.CARD_DELETE_SUCCESSFULLY, HttpStatus.OK.value());
    }

    public SuccessResponse refundProcess(PaymentEntity paymentEntity, OrderItemEntity orderItemEntity) {
        try {

            double redeemedPoints = Optional.ofNullable(orderItemEntity.getRedeemedLoyaltyPoints()).orElse(0.0);
            if (redeemedPoints > 0) {
                SuccessResponse response = loyaltyPointsService.refundPointsForCancelledOrderItem(paymentEntity.getUserId(), orderItemEntity.getOrderEntity().getOrderId(), orderItemEntity.getOrderItemId());
                log.info("Loyalty points cancelled for order item: {}", response);
            }
            double amount = orderItemEntity.getTotalAmount() - redeemedPoints;
            String razorPayPaymentId = paymentEntity.getRazorPayPaymentId();

            if (isNull(razorPayPaymentId) || razorPayPaymentId.isEmpty()) {
                throw new RefundProcessException("Razorpay Payment ID is missing for the refund process.");
            }

            log.info("Refund Process Razor Payment Id: {}  and amount {} ", razorPayPaymentId, amount);
            RazorpayClient razorPayClient = new RazorpayClient(keyID, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put(PaymentConstants.PAYMENT_ID, razorPayPaymentId);
            orderRequest.put(PaymentConstants.AMOUNT, amount * 100);

            log.info("Refund Process Json Object Request : {}", orderRequest);

            Refund refund = razorPayClient.payments.refund(orderRequest);
            log.info("Refund Response : {}", refund);

            if (isNull(refund)) {
                throw new RefundProcessException("Razorpay refund API returned a null response.");
            }

            RefundEntity entity = refundRepository.findByPaymentEntityAndOrderItemEntity(paymentEntity, orderItemEntity);
            entity.setRefundStatus(RefundStatus.INITIATED);
            entity.setRefundType(RefundType.PRODUCT);
            entity.setRefundAmount(amount);
            entity.setRefundIdOrPayoutId(refund.get(PaymentConstants.ID).toString());

            refundRepository.save(entity);

            log.info(Constants.PAYMENT_REFUND_INITIATED_SUCCESSFULLY, refund.get(PaymentConstants.ID), paymentEntity.getOrderEntity().getOrderId());

            return new SuccessResponse(Constants.REFUND_REQUEST_INITIATED, HttpStatus.OK.value());
        } catch (RazorpayException e) {
            log.error("Razorpay refund failed for payment ID: {}", paymentEntity.getRazorPayPaymentId());
            throw new RefundProcessException(String.format(Constants.RAZORPAY_REFUND_FAILED, paymentEntity.getRazorPayPaymentId()));
        }
    }


    @Override
    public SuccessResponse reconcilePendingPayments() {
        List<PaymentEntity> pendingPayments = paymentRepository.findByPaymentStatusAndPaymentMethodNot(PaymentStatus.PENDING, Constants.COD);
        log.info("List Of Pending Payments : {}", pendingPayments);

        SuccessResponse response = new SuccessResponse();

        if (pendingPayments.isEmpty()) {
            response.setMessage(Constants.NO_PAYMENTS_AVAILABLE_FOR_RECONCILE);
            response.setStatusCode(HttpStatus.OK.value());
            log.info("Reconciled Payment Response: {}", response);
            return response;
        }

        RazorpayClient razorPayClient;
        try {
            razorPayClient = new RazorpayClient(keyID, keySecret);
        } catch (RazorpayException e) {
            response.setMessage(Constants.FAILED_TO_INITIALIZE_RAZORPAY);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.error("Error initializing Razorpay client: {}", e.getMessage(), e);
            return response;
        }

        StringBuilder successMessages = new StringBuilder();
        StringBuilder errorMessages = new StringBuilder();

        for (PaymentEntity paymentEntity : pendingPayments) {
            try {
                reconcilePayment(razorPayClient, paymentEntity, successMessages);
            } catch (RazorpayException e) {
                String errorMessage = String.format("Failed to fetch Razorpay payment for paymentId: %s. Error: %s",
                        paymentEntity.getRazorPayPaymentId(), e.getMessage());
                log.error(errorMessage, e);
                errorMessages.append(errorMessage).append("\n");
            } catch (Exception e) {
                String errorMessage = String.format("Unexpected error while reconciling paymentId: %s. Error: %s",
                        paymentEntity.getRazorPayPaymentId(), e.getMessage());
                log.error(errorMessage, e);
                errorMessages.append(errorMessage).append("\n");
            }
        }

        if (!successMessages.isEmpty()) {
            response.setMessage("Reconciliation Summary:\n" + successMessages);
            response.setStatusCode(HttpStatus.OK.value());
        } else {
            response.setMessage("No payments were successfully reconciled.\n" + errorMessages);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        log.info("Reconciled Payment Response: {}", response);
        return response;
    }

    @Override
    public List<SuccessResponse> refundStatusChecking() {
        List<RefundEntity> refundEntities = refundRepository.findAllByRefundStatus(RefundStatus.INITIATED);
        log.info("List of Refund Entity with Initiated Refunds: {}", refundEntities);

        List<SuccessResponse> successResponseList = new ArrayList<>();

        if (refundEntities.isEmpty()) {
            return addNoRefundsResponse(successResponseList);
        }

        for (RefundEntity refundEntity : refundEntities) {
            if(refundEntity.getRefundType().equals(RefundType.PRODUCT)) {
                SuccessResponse response = processRefundEntity(refundEntity);
                successResponseList.add(response);
            } else if(refundEntity.getRefundType().equals(RefundType.POINTS)) {
                RefundResponse response = checkCODRefundStatus(refundEntity.getRefundIdOrPayoutId());
                SuccessResponse successResponse = handleTransferToAccount(response, refundEntity);
                successResponseList.add(successResponse);
            }
        }

        log.info("List Of Refund Response Success: {}", successResponseList);
        return successResponseList;
    }

    @Override
    public SuccessResponse transferToAccount(TransferToAccountRequest request) {
        return paymentServiceHelper.transferToAccount(request);
    }

    @Override
    public String createRazorpayContact(String accountHolderName, String emailAddress, String phoneNumber)
            throws RazorpayDataException {
        log.info("Creating Razorpay contact. Account Holder Name: {}, Email: {}, Phone: {}",
                accountHolderName, emailAddress, phoneNumber);

        String url = PaymentConstants.CONTACT_URL;

        JSONObject requestBody = new JSONObject();
        requestBody.put(PaymentConstants.NAME, accountHolderName);
        requestBody.put(PaymentConstants.EMAIL, emailAddress);
        requestBody.put(PaymentConstants.CONTACT, phoneNumber);
        requestBody.put(PaymentConstants.TYPE, PaymentConstants.CUSTOMER);

        String response = paymentServiceHelper.sendHttpRequest(url, requestBody, HttpMethod.POST, "Failed to create Razorpay contact");

        JSONObject jsonResponse = new JSONObject(response);
        String contactId = jsonResponse.getString(PaymentConstants.ID);
        log.info("Contact created successfully. Contact ID: {}", contactId);
        return contactId;

    }

    @Override
    public String createFundAccount(String razorpayContactId, UserBankDetailsRequest request) {
        try {
            log.info("Creating Fund Account. Razorpay Contact ID: {}, User Bank Details Request: {}",
                    razorpayContactId, request);

            RazorpayClient razorPayClient = new RazorpayClient(keyID, keySecret);

            JSONObject fundAccountRequest = new JSONObject();
            fundAccountRequest.put(PaymentConstants.CONTACT_ID, razorpayContactId);
            fundAccountRequest.put(PaymentConstants.ACCOUNT_TYPE, PaymentConstants.BANK_ACCOUNT_TYPE);

            JSONObject bankAccount = new JSONObject();
            bankAccount.put(PaymentConstants.NAME, request.getAccountHolderName());
            bankAccount.put(PaymentConstants.IFSC, request.getBankIfscCode());
            bankAccount.put(PaymentConstants.ACCOUNT_NUMBER, request.getAccountNumber());
            fundAccountRequest.put(PaymentConstants.BANK_ACCOUNT_TYPE, bankAccount);

            FundAccount fundAccount = razorPayClient.fundAccount.create(fundAccountRequest);
            log.info("Fund Account created successfully: {}", fundAccount);
            return fundAccount.get("id").toString();
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay client. Error: {}", e.getMessage());
            throw new RazorpayDataException(Constants.CREATE_FUND_ACCOUNT_ERROR);
        }
    }

    @Override
    public List<SuccessResponse> initiateRefund() {
        List<SuccessResponse> successResponses = new ArrayList<>();
        List<OrderItemEntity> orderItemEntities = orderItemRepository.findOrderItemsByReturnStatus(ReturnStatus.RETURN_COMPLETED);

        log.info("List of Order Items: {}", orderItemEntities);
        if (isNull(orderItemEntities) || orderItemEntities.isEmpty()) {
            log.info("No completed return order items found");
            return successResponses;
        }

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            try {
                SuccessResponse response = processRefund(orderItemEntity);
                if (response != null) {
                    successResponses.add(response);
                }
            } catch (Exception e) {
                log.error("Error processing refund for OrderItem ID: {}", orderItemEntity.getOrderItemId(), e);
                throw new RefundProcessException("Error processing refund for OrderItem ID: " + orderItemEntity.getOrderItemId(), e);
            }
        }

        return successResponses;
    }

    private SuccessResponse processRefund(OrderItemEntity orderItemEntity) {
        OrderEntity orderEntity = orderItemEntity.getOrderEntity();
        if (isNull(orderEntity)) {
            log.warn("OrderEntity is null for OrderItem: {}", orderItemEntity.getOrderItemId());
            return new SuccessResponse("OrderEntity is null for OrderItem: {}" + orderItemEntity.getOrderItemId(), HttpStatus.NOT_FOUND.value());
        }

        PaymentEntity paymentEntity = orderEntity.getPaymentEntity();
        if (isNull(paymentEntity)) {
            log.warn("PaymentEntity is null for Order: {}", orderEntity.getOrderId());
            return new SuccessResponse("PaymentEntity is null for Order: {}" + orderEntity.getOrderId(), HttpStatus.NOT_FOUND.value());
        }

        String paymentMethod = paymentEntity.getPaymentMethod();
        if (Constants.RAZORPAY.equals(paymentMethod)) {
            return handleRazorpayRefund(paymentEntity, orderItemEntity);
        } else if (Constants.COD.equals(paymentMethod)) {
            return handleCODRefund(orderEntity, orderItemEntity);
        } else {
            log.warn("Unsupported payment method: {}", paymentMethod);
            return new SuccessResponse("Unsupported payment method: " + paymentMethod, HttpStatus.BAD_REQUEST.value());
        }
    }

    private SuccessResponse handleRazorpayRefund(PaymentEntity paymentEntity, OrderItemEntity orderItemEntity) {
        return refundProcess(paymentEntity, orderItemEntity);
    }

    private SuccessResponse handleCODRefund(OrderEntity orderEntity, OrderItemEntity orderItemEntity) {
        TransferToAccountRequest request = new TransferToAccountRequest();
        request.setUserBankId(orderItemEntity.getUserBankId());
        request.setOrderId(orderEntity.getOrderId());
        request.setOrderItemId(orderItemEntity.getOrderItemId());

        return paymentServiceHelper.transferToAccount(request);
    }


    private void reconcilePayment(RazorpayClient razorPayClient, PaymentEntity paymentEntity, StringBuilder successMessages) throws RazorpayException {
        Payment razorpayPayment = razorPayClient.payments.fetch(paymentEntity.getRazorPayPaymentId());
        String status = razorpayPayment.get(PaymentConstants.STATUS);

        if (PaymentConstants.CAPTURED.equals(status)) {
            paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(paymentEntity);

            OrderEntity orderEntity = paymentEntity.getOrderEntity();
            log.info("Order Entity Response : {}", orderEntity);
            if (!isNull(orderEntity)) {
                orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
                orderRepository.save(orderEntity);
                successMessages.append(String.format("Reconciled paymentId: %s, orderId: %s%n",
                        paymentEntity.getPaymentId(), orderEntity.getOrderId()));
            } else {
                successMessages.append(String.format("Reconciled paymentId: %s (No associated order)%n",
                        paymentEntity.getPaymentId()));
            }
            log.info("Successfully reconciled payment: {}", paymentEntity.getPaymentId());
        } else {
            log.warn("Payment status is not 'captured' for paymentId: {}", paymentEntity.getPaymentId());
        }
    }

    private Order createRazorpayOrder(RazorpayClient razorpayClient, Double amount, String receiptPrefix) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();

        int amountIn = BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        orderRequest.put(PaymentConstants.AMOUNT, amountIn);
        orderRequest.put(PaymentConstants.CURRENCY, PaymentConstants.INR);
        orderRequest.put(PaymentConstants.RECEIPT, receiptPrefix + System.currentTimeMillis());
        return razorpayClient.orders.create(orderRequest);
    }

    private void validateUser(String userId) {
        UserEntity userEntity = userRepository.findUserByUserId(userId);
        if (isNull(userEntity)) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }
    }

    private OrderEntity validateOrder(String orderId) {
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId);
        if (isNull(orderEntity)) {
            throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
        }
        return orderEntity;
    }

    private PaymentEntity validateExistingPayment(String orderId) {
        Optional<PaymentEntity> existingPayment = paymentRepository.findByOrderEntityOrderId(orderId);
        if (existingPayment.isEmpty()) {
            throw new PaymentNotFoundException(Constants.PAYMENT_NOT_FOUND);
        }
        return existingPayment.get();
    }

    private PaymentResponse buildPaymentResponse(String message, String razorPayOrderId, LocalDateTime paymentDate) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus(Constants.SUCCESS);
        response.setMessage(message);
        response.setRazorPayOrderId(razorPayOrderId);
        response.setPaymentDate(localDateFormat.format(paymentDate));
        return response;
    }

    private List<SuccessResponse> addNoRefundsResponse(List<SuccessResponse> successResponseList) {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage(Constants.NO_REFUND_INITIATED_PAYMENTS_AVAILABLE);
        successResponse.setStatusCode(HttpStatus.OK.value());
        log.info(Constants.REFUND_STATUS_MSG, successResponse);
        successResponseList.add(successResponse);
        return successResponseList;
    }

    private SuccessResponse processRefundEntity(RefundEntity refundEntity) {
        SuccessResponse successResponse = new SuccessResponse();
        try {
            String paymentMethod = refundEntity.getPaymentEntity().getPaymentMethod();
            log.info("Processing refund for Payment Method: {}", paymentMethod);

            RefundResponse refundResponse;
            if (Constants.RAZORPAY.equalsIgnoreCase(paymentMethod)) {
                refundResponse = checkRazorpayRefundStatus(refundEntity.getRefundIdOrPayoutId());
                handleRazorpayRefund(refundEntity, refundResponse, successResponse);
            } else if (Constants.COD.equalsIgnoreCase(paymentMethod)) {
                refundResponse = checkCODRefundStatus(refundEntity.getRefundIdOrPayoutId());
                handleCODRefund(refundEntity, refundResponse, successResponse);
            } else {
                log.warn("Unsupported payment method: {}", paymentMethod);
                successResponse.setMessage("Unsupported payment method: " + paymentMethod);
                successResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return successResponse;
            }
        } catch (Exception e) {
            log.error("Error processing refund for Refund ID: {}", refundEntity.getRefundIdOrPayoutId(), e);
            successResponse.setMessage("Error processing refund for Refund ID: " + refundEntity.getRefundIdOrPayoutId());
            successResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return successResponse;
    }

    private void handleRazorpayRefund(RefundEntity refundEntity, RefundResponse refundResponse, SuccessResponse successResponse) {
        if (validateRazorpayRefundData(refundEntity, refundResponse)) {
            updateRefundEntity(refundEntity, refundResponse);
            successResponse.setMessage("Refund Status Completed for Refund ID: "
                    + refundEntity.getRefundIdOrPayoutId() + ", RazorPay Payment ID: "
                    + refundEntity.getPaymentEntity().getRazorPayPaymentId());
        } else {
            successResponse.setMessage("Refund Status Not Completed for Refund ID: "
                    + refundEntity.getRefundIdOrPayoutId() + ", RazorPay Payment ID: "
                    + refundEntity.getPaymentEntity().getRazorPayPaymentId());
        }
        successResponse.setStatusCode(HttpStatus.OK.value());
        log.info(Constants.REFUND_STATUS_MSG, successResponse);
    }

    private void handleCODRefund(RefundEntity refundEntity, RefundResponse refundResponse, SuccessResponse successResponse) {
        if (validateCODRefundData(refundEntity, refundResponse)) {
            updateRefundEntity(refundEntity, refundResponse);
            successResponse.setMessage("Refund Status Completed for Refund ID: "
                    + refundEntity.getRefundIdOrPayoutId() + ", COD Payment ID: "
                    + refundEntity.getPaymentEntity().getRazorPayPaymentId());
        } else {
            successResponse.setMessage("Refund Status Not Completed for Refund ID: "
                    + refundEntity.getRefundIdOrPayoutId() + ", COD Payment ID: "
                    + refundEntity.getPaymentEntity().getRazorPayPaymentId());
        }
        successResponse.setStatusCode(HttpStatus.OK.value());
        log.info(Constants.REFUND_STATUS_MSG, successResponse);
    }

    private RefundResponse checkCODRefundStatus(String refundIdOrPayoutId) {

        String url = PaymentConstants.PAYOUT_REFUND_CHECKING_URL + refundIdOrPayoutId;
        String response = paymentServiceHelper.sendHttpRequest(url, null, HttpMethod.GET, "Failed to Retrieve the response");
        JSONObject jsonObject = new JSONObject(response);

        RefundResponse refundResponse = new RefundResponse();
        double amount = jsonObject.optDouble(PaymentConstants.AMOUNT) / 100.0;
        refundResponse.setRefundAmount(amount);
        refundResponse.setRefundIdOrPayoutId(refundIdOrPayoutId);
        refundResponse.setRefundDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        refundResponse.setRefundStatus(jsonObject.getString(PaymentConstants.STATUS));

        log.info("Refund Response For the COD Payment : {}", refundResponse);
        return refundResponse;
    }

    private RefundResponse checkRazorpayRefundStatus(String refundIdOrPayoutId) {
        try {
            RazorpayClient razorPayClient = new RazorpayClient(keyID, keySecret);
            Refund refund = razorPayClient.refunds.fetch(refundIdOrPayoutId);
            log.info("Refund Response For the RazorpayRefundId :{}, response : {}  ", refundIdOrPayoutId, refund);
            RefundResponse refundResponse = new RefundResponse();

            if (refund != null && PaymentConstants.PROCESSED.equals(refund.get(PaymentConstants.STATUS))) {
                Integer amount = refund.get(PaymentConstants.AMOUNT);
                double refundAmountInRupees = amount / 100.0;

                refundResponse.setRefundAmount(refundAmountInRupees);
                refundResponse.setRefundDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
                refundResponse.setRazorPayPaymentId(refund.get(PaymentConstants.PAYMENT_ID));
                refundResponse.setRefundRazorpayId(refund.get(PaymentConstants.ID));
                refundResponse.setRefundStatus(refund.get(PaymentConstants.STATUS));
                refundResponse.setRefundIdOrPayoutId(refundIdOrPayoutId);
                return refundResponse;
            } else {
                assert refund != null;
                Integer amount = refund.get(PaymentConstants.AMOUNT);
                double refundAmountInRupees = amount / 100.0;

                refundResponse.setRefundAmount(refundAmountInRupees);
                refundResponse.setRefundDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
                refundResponse.setRazorPayPaymentId(refund.get(PaymentConstants.PAYMENT_ID));
                refundResponse.setRefundRazorpayId(refund.get(PaymentConstants.ID));
                refundResponse.setRefundStatus(refund.get(PaymentConstants.STATUS));
                refundResponse.setRefundIdOrPayoutId(refundIdOrPayoutId);
            }

            log.info("Refund Response for the RAZORPAY Payment: {}", refundResponse);
            return refundResponse;
        } catch (RazorpayException e) {
            log.error("Error while fetching refund status: {}", e.getMessage());
            return new RefundResponse();
        }
    }

    private boolean validateRazorpayRefundData(RefundEntity refundEntity, RefundResponse refundResponse) {
        return refundResponse.getRefundRazorpayId().equals(refundEntity.getRefundId()) &&
                refundResponse.getRazorPayPaymentId().equals(refundEntity.getPaymentEntity().getRazorPayPaymentId()) &&
                refundResponse.getRefundAmount().equals(refundEntity.getRefundAmount()) &&
                refundResponse.getRefundStatus().equals(PaymentConstants.PROCESSED);
    }

    private boolean validateCODRefundData(RefundEntity refundEntity, RefundResponse refundResponse) {
        return refundResponse.getRefundIdOrPayoutId().equals(refundEntity.getRefundIdOrPayoutId()) &&
                refundResponse.getRefundAmount().equals(refundEntity.getRefundAmount()) &&
                refundResponse.getRefundStatus().equals(PaymentConstants.PROCESSED);
    }

    private void updateRefundEntity(RefundEntity refundEntity, RefundResponse refundResponse) {
        refundEntity.setRefundStatus(RefundStatus.COMPLETED);
        refundEntity.setRefundDate(refundResponse.getRefundDate());
        refundRepository.save(refundEntity);
    }

    private SuccessResponse handleTransferToAccount(RefundResponse response, RefundEntity refundEntity) {
        SuccessResponse successResponse = new SuccessResponse();
        if(validateCODRefundData(refundEntity, response)) {
            updateRefundEntity(refundEntity, response);
            successResponse.setMessage("Refund Status Completed for Refund Transfer To Account ID: "
                    + refundEntity.getRefundIdOrPayoutId());
        } else {
            successResponse.setMessage("Refund Status Not Completed for Transfer To Account Refund ID: "
                    + refundEntity.getRefundIdOrPayoutId());
        }
        successResponse.setStatusCode(HttpStatus.OK.value());
        log.info(Constants.REFUND_STATUS_MSG, successResponse);

        log.info("Transfer To Account Response for the Points : {}", successResponse);
        return successResponse;
    }

    public void sendOrderConfirmation(UserEntity userEntity, OrderEntity orderEntity) {
        for (OrderItemEntity orderItem : orderEntity.getOrderItemEntities()) {
            String orderItemId = orderItem.getOrderItemId();
            whatsAppService.notifyOrderConfirmation(userEntity.getPhoneNumber(), userEntity.getFirstName(), orderItemId, String.valueOf(LocalDate.now()));
        }
    }
}