package com.cordestitch.service.serviceimplementation.webhook;

import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.request.order.CancelOrderRequest;
import com.cordestitch.request.webhook.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.order.CancelOrderResponse;
import com.cordestitch.service.service.order.OrderService;
import com.cordestitch.service.service.webhook.WebHookService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.WhatsAppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebHookServiceImplementation implements WebHookService {

    private final WhatsAppService whatsAppService;

    private final OrderService orderService;

    private final OrderItemRepository orderItemRepository;

    @Value("${webhook.verify.token}")
    private String accessToken;

    @Override
    public String verifyWebhook(String mode, String token, String challenge) {
        if (WhatsAppConstants.SUBSCRIBE.equals(mode) && accessToken.equals(token)) {
            log.info("Verify Web Hook Request : mode {}, token {}, challenge {}",
                    mode, token, challenge);
            return challenge;
        }
        return null;
    }

    @Override
    public SuccessResponse handleWebhookEvent(WebHookEventRequest webhookRequest) {
        log.info("Received Webhook Event: {}", webhookRequest);
        try {
            if (isNull(webhookRequest.getEntry())) {
                log.warn("Webhook request or entry list is null.");
                return new SuccessResponse(Constants.SUCCESS, HttpStatus.OK.value());
            }

            webhookRequest.getEntry().forEach(entry -> entry.getChanges()
                    .forEach(change -> {
                        ValueRequest value = change.getValue();
                        if (WhatsAppConstants.WEBHOOK_MESSAGE.equals(change.getField())) {
                            processUserReplies(value);
                        } else
                            log.warn("Unhandled field type: {}", change.getField());
                    }));

            log.info("Sending success response to meta");
        } catch (Exception e) {
            String recipientNumber = webhookRequest.getEntry().getFirst().getChanges().getFirst().getValue().getMessages().getFirst().getFrom();
            whatsAppService.sendSomeThingWentWrong(recipientNumber, WhatsAppConstants.TECHNICAL_ISSUE);
        }
        return new SuccessResponse(Constants.SUCCESS, HttpStatus.OK.value());
    }

    private void processUserReplies(ValueRequest value) {
        if (!isNull(value.getMessages()) && !value.getMessages().isEmpty()) {
            for (MessageRequest message : value.getMessages()) {
                handleUserReply(message, value);
            }
        } else
            log.warn("No user messages found.");
    }

    private void handleUserReply(MessageRequest message, ValueRequest value) {

        String messageId = message.getId();
        String recipientNumber = message.getFrom();
        String messageType = message.getType();
        log.info("New Message - ID: {}, Type: {}, From: {}", messageId, messageType, recipientNumber);

        if (!isNull(messageId))
            whatsAppService.markAsRead(value.getMessagingProduct(), WhatsAppConstants.READ, messageId);

        switch (messageType) {
            case WhatsAppConstants.TEXT:
                handleTextMessage(recipientNumber);
                break;
            case WhatsAppConstants.BUTTON:
                handleButtonMessage(message, recipientNumber);
                break;
            case WhatsAppConstants.INTERACTIVE:
                handleInteractiveMessage(message, recipientNumber);
                break;
            default:
                log.warn("Unknown message type: {}", message.getType());
                break;
        }
    }

    private void handleTextMessage(String recipientNumber) {
        whatsAppService.sendWelcomeMessage(recipientNumber);
        log.info("Whatsapp message initiated for the text message");
    }

    private void handleButtonMessage(MessageRequest message, String recipientNumber) {

        String buttonMessageBody = message.getButton().getText();
        log.info("Found user button message: {}", buttonMessageBody);
        String payLoadData = message.getButton().getPayload();

        switch (buttonMessageBody) {
            case WhatsAppConstants.CANCEL_ORDER_BUTTON:
                whatsAppService.cancelOrderActionRequest(recipientNumber, payLoadData);
                break;
            case WhatsAppConstants.TRACK_ORDER_BUTTON:
                sendOrderTrackingUpdates(message, recipientNumber);
                break;
            default:
                log.info("Button type unknown {}", buttonMessageBody);
                break;
        }
    }

    private void handleInteractiveMessage(MessageRequest message, String recipientNumber) {

        InterActiveRequest interactive = message.getInteractive();

        if (interactive.getListReply() != null) {
            handleListReply(interactive.getListReply(), recipientNumber);
        } else if (interactive.getButtonReply() != null) {
            handleButtonReply(interactive.getButtonReply(), recipientNumber);
        }
    }

    private void handleButtonReply(ListReplyRequest buttonReply, String recipientNumber) {

        log.info("Found user interactive button reply: {}", buttonReply.getTitle());
        String interactiveButtonReplyId = buttonReply.getId();

        switch (buttonReply.getTitle()) {
            case WhatsAppConstants.YES_BUTTON:
                whatsAppService.sendCancellationReasons(recipientNumber, interactiveButtonReplyId);
                break;
            case WhatsAppConstants.NO_BUTTON, WhatsAppConstants.DO_NOT_CANCEL_BUTTON:
                whatsAppService.sendTextMessage(recipientNumber, WhatsAppConstants.CANCELLATION_NOT_REQUIRED_MESSAGE);
                break;
            case WhatsAppConstants.START_BUTTON:
                whatsAppService.sendCatalogAndSupport(recipientNumber);
                break;
            case WhatsAppConstants.STOP_BUTTON:
                whatsAppService.sendTextMessage(recipientNumber, WhatsAppConstants.STOP_BUTTON_RESPONSE);
                break;
            case WhatsAppConstants.TOP_CATEGORY:
                whatsAppService.sendTopCategoryInteractiveList(recipientNumber);
                break;
            case WhatsAppConstants.TRENDING_PRODUCT:
                whatsAppService.sendTextMessage(recipientNumber, WhatsAppConstants.TRENDING_PRODUCT_MESSAGE_BODY);
                break;
            case WhatsAppConstants.CONTACT_US:
                whatsAppService.sendTextMessage(recipientNumber, WhatsAppConstants.CONTACT_US_MESSAGE_BODY);
                break;
            default:
                log.info("Unknown interactive reply {}", buttonReply.getTitle());

        }
    }

    private void handleListReply(ListReplyRequest listReply, String recipientNumber) {

        String listReplyTitle = listReply.getTitle();
        String listReplyId = listReply.getId();

        log.info("Found user interactive list reply: {}", listReplyTitle);

        if (isCancellationReason(listReplyTitle)) {
            handleCancellation(listReplyTitle, listReplyId, recipientNumber);
        } else {
            sendCategoryTypes(listReplyTitle, recipientNumber);
        }
    }

    private void handleCancellation(String listReplyTitle, String listReplyId, String recipientNumber) {
        try {
            CancelOrderResponse cancelOrderResponse = processCancellation(listReplyTitle, listReplyId);
            sendOrderCancelledNotification(cancelOrderResponse, recipientNumber);
        } catch (Exception e) {
            log.error("Cancellation process not completed.");
            whatsAppService.sendSomeThingWentWrong(recipientNumber, WhatsAppConstants.WHILE_CANCELLING_ORDER);
        }
    }

    private void sendCategoryTypes(String listReplyTitle, String recipientNumber) {
        String messageBody = WhatsAppConstants.CATEGORY_TYPES.get(listReplyTitle);
        if (!isNull(messageBody)) {
            whatsAppService.sendTextMessage(recipientNumber, messageBody);
        } else {
            log.warn("No message body found for list title: {}", listReplyTitle);
        }
    }

    private void sendOrderCancelledNotification(CancelOrderResponse cancelOrderResponse, String recipientNumber) {
        OrderStatus orderStatus = cancelOrderResponse.getOrderStatus();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Notification delay interrupted for user", e);
        }
        if (orderStatus.equals(OrderStatus.CANCELED)) {
            whatsAppService.notifyOrderCancelledFromWhatsapp(recipientNumber);
        }
    }

    private void sendOrderTrackingUpdates(MessageRequest message, String recipientNumber) {

        String orderItemId = extractOrderItemId(message.getButton().getPayload());
        log.info("Order Item Id :{}", orderItemId);

        OrderItemEntity orderItemEntity = orderItemRepository.findByOrderItemId(orderItemId);
        String userName = orderItemEntity.getOrderEntity().getUserEntity().getFirstName();
        DeliveryStatus deliveryStatus = orderItemEntity.getDeliveryStatus();

        whatsAppService.sendOrderStatusMessage(recipientNumber, userName, orderItemId, String.valueOf(LocalDate.now()), deliveryStatus);

    }

    private CancelOrderResponse processCancellation(String orderCancellationReason, String cancellationOrderItemId) {

        String orderItemId = extractOrderItemId(cancellationOrderItemId);
        log.info("Order item id {}", orderItemId);

        OrderItemEntity orderItemEntity = orderItemRepository.findByOrderItemId(orderItemId);
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        if (orderItemEntity.getReturnDaysPolicy() > 0) {
            cancelOrderRequest.setOrderId(orderItemEntity.getOrderEntity().getOrderId());
            cancelOrderRequest.setOrderItemId(orderItemId);
            cancelOrderRequest.setReason(orderCancellationReason);
            cancelOrderRequest.setUserId(orderItemEntity.getOrderEntity().getUserEntity().getUserId());
            cancelOrderRequest.setIsRefund(true);
        }
        return orderService.cancelOrder(cancelOrderRequest);
    }

    private String extractOrderItemId(String orderItemId) {
        Pattern pattern = Pattern.compile(Constants.ORDER_ITEM_ID + "\\d+");
        Matcher matcher = pattern.matcher(orderItemId);
        log.info("Matching data {} ", matcher);

        return matcher.find() ? matcher.group(0) : "order Item id not found";
    }


    private boolean isCancellationReason(String listReplyTitle) {
        Set<String> cancellationReasons = new HashSet<>(Arrays.asList(
                WhatsAppConstants.IN_APPROPRIATE_SIZE,
                WhatsAppConstants.ORDERED_BY_MISTAKE,
                WhatsAppConstants.CHANGED_MIND,
                WhatsAppConstants.FOUND_BETTER_PRICE,
                WhatsAppConstants.NO_LONGER_NEEDED,
                WhatsAppConstants.FOUND_OTHER,
                WhatsAppConstants.WRONG_COLOR,
                WhatsAppConstants.QUALITY_CONCERN
        ));
        return cancellationReasons.contains(listReplyTitle);
    }
}