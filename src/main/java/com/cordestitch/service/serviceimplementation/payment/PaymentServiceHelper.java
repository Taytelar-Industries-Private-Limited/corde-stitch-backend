package com.cordestitch.service.serviceimplementation.payment;

import com.razorpay.FundAccount;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.user.UserBankAccountEntity;
import com.cordestitch.enums.RefundStatus;
import com.cordestitch.enums.RefundType;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.exception.order.OrderNotFoundException;
import com.cordestitch.exception.payment.InvalidAccountNumberException;
import com.cordestitch.exception.payment.RazorpayDataException;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.payment.RefundRepository;
import com.cordestitch.repository.user.UserBankDetailsRepository;
import com.cordestitch.request.payment.TransferToAccountRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import com.cordestitch.util.PaymentConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;

import static java.util.Objects.isNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceHelper {

    private final OrderRepository orderRepository;

    private final RefundRepository refundRepository;

    private final UserBankDetailsRepository userBankDetailsRepository;

    private final Generator generator;

    private final LoyaltyPointsService loyaltyPointsService;

    @Value("${razorpay.api.key.id}")
    private String keyID;

    @Value("${razorpay.api.key.secret}")
    private String keySecret;

    @Value("${razorpay.x.account.number}")
    private String razorpayAccountNumber;


    @Transactional
    public SuccessResponse transferToAccount(TransferToAccountRequest request) {
        try {
            log.info("Transfer to Account Request : {}", request);

            UserBankAccountEntity entity = userBankDetailsRepository.findByUserBankId(request.getUserBankId());
            log.info("User Bank Account Details : {}", entity);
            if (isNull(entity)) {
                log.error(Constants.INVALID_ACCOUNT_NUMBER);
                throw new InvalidAccountNumberException(Constants.INVALID_ACCOUNT_NUMBER);
            }

            Double amount = 0.0;
            RefundEntity refundEntity = new RefundEntity();
            refundEntity.setRefundId(generator.generateId(Constants.REFUND_ID));
            refundEntity.setRefundType(RefundType.POINTS);

            if (request.getOrderId() != null && request.getOrderItemId() != null) {
                OrderEntity orderEntity = orderRepository.findByOrderId(request.getOrderId());
                log.info("Order entity Data : {}", orderEntity);

                if (isNull(orderEntity)) {
                    log.error(Constants.ORDER_NOT_FOUND);
                    throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
                }

                amount = fetchOrderItemAmount(orderEntity, request);

                log.info("Fetched from OrderItem Entity amount to be transferred to Account : {}", amount);

                double redemptionPoints = fetchOrderItemRedemptionPoints(orderEntity, request);

                log.info("Fetched from OrderItem Entity Redemption Points : {}", redemptionPoints);

                double redeemedPoints = Optional.of(redemptionPoints).orElse(0.0);

                refundLoyaltyPoints(orderEntity, redeemedPoints, request);

                amount = amount - redeemedPoints;
                log.info("Final amount to be transferred to Account after loyalty points cancellation : {}", amount);

                refundEntity.setRefundType(RefundType.PRODUCT);
                refundEntity.setOrderItemEntity(orderEntity.getOrderItemEntities().stream()
                        .filter(orderItemEntity -> orderItemEntity.getOrderItemId().equals(request.getOrderItemId()))
                        .findFirst()
                        .orElse(null));

            } else if (request.getAmount() != null && request.getAmount() > 0.0) {
                amount = request.getAmount();
                log.info("Getting from the request amount To Be Transferred To Account : {}", amount);
            }

            refundEntity.setRefundAmount(amount);
            refundEntity.setRefundDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
            refundEntity.setRefundStatus(RefundStatus.REQUESTED);
            refundRepository.save(refundEntity);

            RazorpayClient razorPayClient = new RazorpayClient(keyID, keySecret);

            FundAccount fundAccount = razorPayClient.fundAccount.fetch(entity.getRazorpayFundAccountId());
            log.info("Fetching fund account for the id {} and data :{}", entity.getRazorpayFundAccountId(), fundAccount);

            JSONObject bankAccount = fundAccount.get(PaymentConstants.BANK_ACCOUNT_TYPE);

            String accountNumber;
            String accountHolderName;
            String bankIfscCode;

            if (bankAccount != null) {
                accountNumber = bankAccount.get(PaymentConstants.ACCOUNT_NUMBER).toString();
                accountHolderName = bankAccount.get(PaymentConstants.NAME).toString();
                bankIfscCode = bankAccount.get(PaymentConstants.IFSC).toString();
            } else {
                log.error(Constants.BANK_ACCOUNT_DETAILS_MISSING_MSG);
                throw new RazorpayDataException(Constants.BANK_ACCOUNT_DETAILS_MISSING_MSG);
            }
            log.info("Account Number : {}, Account Holder Name : {}, Bank IFSC code {}", accountNumber, accountHolderName, bankIfscCode);

            boolean isValid = validateFundAccount(accountNumber, accountHolderName, bankIfscCode, entity);
            if (isValid) {
                JSONObject payoutObject = new JSONObject();
                payoutObject.put(PaymentConstants.ACCOUNT_NUMBER, razorpayAccountNumber);
                payoutObject.put(PaymentConstants.FUND_ACCOUNT_ID, entity.getRazorpayFundAccountId());
                payoutObject.put(PaymentConstants.AMOUNT, amount * 100);
                payoutObject.put(PaymentConstants.CURRENCY, PaymentConstants.INR);
                payoutObject.put(PaymentConstants.MODE, PaymentConstants.NEFT);
                payoutObject.put(PaymentConstants.QUEUE_IF_LOW_BALANCE, true);

                String purpose = refundEntity.getRefundType() == RefundType.PRODUCT
                        ? PaymentConstants.REFUND
                        : PaymentConstants.CASHBACK;
                payoutObject.put(PaymentConstants.PURPOSE, purpose);

                String payoutId = initiatePayout(payoutObject);
                refundEntity.setRefundIdOrPayoutId(payoutId);
                refundEntity.setRefundStatus(RefundStatus.INITIATED);
                refundRepository.save(refundEntity);
            }

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay client: {}", e.getMessage(), e);
            throw new RazorpayDataException(Constants.RAZORPAY_CLIENT_CREATION_FAILED);
        } catch (OrderNotFoundException e) {
            log.error("Error finding Order: {}", e.getMessage(), e);
            throw e;
        }catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);
            throw new RazorpayDataException(Constants.REFUND_PROCESSING_FAILED);
        }

        SuccessResponse successResponse = new SuccessResponse(Constants.TRANSFER_TO_ACCOUNT_REQUEST_INITIATED, HttpStatus.OK.value());
        log.info("Transfer To Account Response : {}", successResponse);
        return successResponse;
    }

    private void refundLoyaltyPoints(OrderEntity orderEntity, double redeemedPoints, TransferToAccountRequest request) {
        if (redeemedPoints > 0) {
            SuccessResponse response = loyaltyPointsService.refundPointsForCancelledOrderItem(orderEntity.getUserEntity().getUserId(), orderEntity.getOrderId(), request.getOrderItemId());
            log.info("Cancelled loyalty points for order item: {}", response);
        }
    }

    private double fetchOrderItemRedemptionPoints(OrderEntity orderEntity, TransferToAccountRequest request) {
        return orderEntity.getOrderItemEntities().stream()
                .filter(orderItemEntity -> orderItemEntity.getOrderItemId().equals(request.getOrderItemId()))
                .map(OrderItemEntity::getRedeemedLoyaltyPoints)
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));
    }

    private Double fetchOrderItemAmount(OrderEntity orderEntity, TransferToAccountRequest request) {
        return orderEntity.getOrderItemEntities().stream()
                .filter(orderItemEntity -> orderItemEntity.getOrderItemId().equals(request.getOrderItemId()))
                .map(OrderItemEntity::getTotalAmount)
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));
    }

    private String initiatePayout(JSONObject payoutObject) {
        log.info("Initiating Payout request: {}", payoutObject);
        String url = PaymentConstants.PAYOUT_URL;
        String response = sendHttpRequest(url, payoutObject, HttpMethod.POST,"Failed to initiate payout");

        JSONObject jsonResponse = new JSONObject(response);
        String payoutId = jsonResponse.getString(PaymentConstants.ID);
        log.info("Payout created successfully. Payout ID: {}", payoutId);
        return payoutId;
    }
    private boolean validateFundAccount(String accountNumber, String accountHolderName, String bankIfscCode, UserBankAccountEntity entity) {
        return entity.getAccountHolderName().equals(accountHolderName) &&
                entity.getAccountNumber().equals(accountNumber) &&
                entity.getBankIfscCode().equals(bankIfscCode);
    }

    public String sendHttpRequest(String url, JSONObject payload, HttpMethod httpMethod, String errorMessage) throws RazorpayDataException {
        try {
            log.info("Sending request to URL: {}, Payload: {}", url, payload);
            String auth = Base64.getEncoder().encodeToString((keyID + ":" + keySecret).getBytes());

            HttpRequest.BodyPublisher bodyPublisher = payload != null
                    ? HttpRequest.BodyPublishers.ofString(payload.toString())
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .method(String.valueOf(httpMethod), bodyPublisher)
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201 && response.statusCode() != 200) {
                log.error("{} Status Code: {}, Response Body: {}", errorMessage, response.statusCode(), response.body());
                throw new RazorpayDataException(errorMessage);
            }

            log.info("Response: {}", response.body());
            return response.body();

        } catch (HttpTimeoutException timeoutException) {
            log.error(Constants.RAZORPAY_CONTACT_TIMEOUT, timeoutException);
            throw new RazorpayDataException(Constants.RAZORPAY_CONTACT_TIMEOUT);

        } catch (IOException ioException) {
            log.error(Constants.RAZORPAY_CONTACT_IO_ERROR, ioException);
            throw new RazorpayDataException(Constants.RAZORPAY_CONTACT_IO_ERROR);

        } catch (InterruptedException ie) {
            log.error(Constants.RAZORPAY_CONTACT_TIMEOUT, ie);
            Thread.currentThread().interrupt();
            throw new RazorpayDataException(Constants.RAZORPAY_CONTACT_TIMEOUT);

        } catch (Exception e) {
            log.error(Constants.RAZORPAY_CONTACT_UNEXPECTED_ERROR, e);
            throw new RazorpayDataException(Constants.RAZORPAY_CONTACT_UNEXPECTED_ERROR);
        }
    }
}
