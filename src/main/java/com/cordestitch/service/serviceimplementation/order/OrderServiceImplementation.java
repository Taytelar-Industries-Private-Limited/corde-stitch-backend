package com.cordestitch.service.serviceimplementation.order;

import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsTransactionEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.order.ReturnEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.product.ColorQuantity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.StockQuantity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.enums.PaymentStatus;
import com.cordestitch.exception.order.*;
import com.cordestitch.exception.payment.PaymentNotFoundException;
import com.cordestitch.exception.payment.RefundProcessException;
import com.cordestitch.exception.product.ProductNotFoundException;
import com.cordestitch.exception.user.AddressNotFoundException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.cart.CartRepository;
import com.cordestitch.repository.loyalty.LoyaltyPointsRepository;
import com.cordestitch.repository.loyalty.LoyaltyPointsTransactionRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.order.ReturnRepository;
import com.cordestitch.repository.payment.PaymentRepository;
import com.cordestitch.repository.payment.RefundRepository;
import com.cordestitch.repository.product.ColorQuantityRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.user.AddressRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.order.*;
import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.order.*;
import com.cordestitch.response.product.ProductResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.service.service.order.OrderService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.service.serviceimplementation.payment.PaymentServiceImpl;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImplementation implements OrderService {

    private final PaymentServiceImpl paymentServiceImpl;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    private final ColorQuantityRepository colorQuantityRepository;

    private final PaymentRepository paymentRepository;

    private final AddressRepository addressRepository;

    private final ReturnRepository returnRepository;

    private final LoyaltyPointsService loyaltyPointsService;

    private final OrderServiceMappingHelper orderServiceMappingHelper;

    private final Generator generator;

    private final CacheManager cacheManager;

    private final CartRepository cartRepository;

    private final RefundRepository refundRepository;

    private final MongoTemplate mongoTemplate;

    private final LoyaltyPointsRepository loyaltyPointsRepository;

    private final LoyaltyPointsTransactionRepository loyaltyPointsTransactionRepository;

    private final WhatsAppService whatsAppService;
    private static final String PRODUCTS_CACHE_NAME = "productsCache";
    private static final String PRODUCT_CACHE_KEY = "listAllProduct";
    private static final String REFUND = "refund";
    private static final String EXCHANGE = "exchange";


    @Transactional
    @Override
    public PlaceAnOrderResponse placeAnOrder(OrderRequest orderRequest) {

        if ((isNull(orderRequest.getOrderItemRequests()) || orderRequest.getOrderItemRequests().isEmpty())) {
            throw new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND);
        }

        try {
            log.info("Place an order request: {}", orderRequest);
            UserEntity userEntity = userRepository.findUserByUserId(orderRequest.getUserId());
            if (isNull(userEntity)) {
                throw new UserNotFoundException(Constants.USER_NOT_FOUND);
            }

            OrderEntity orderEntity = getNewOrderEntity(orderRequest, userEntity);
            orderRepository.save(orderEntity);
            log.info("Order created: {}", orderEntity);

            List<OrderItemEntity> orderItems = getNewOrderItemEntities(orderRequest.getOrderItemRequests(), orderEntity);
            if (!orderItems.isEmpty()) {
                orderItemRepository.saveAll(orderItems);
                log.info("Order items created: {}", orderItems);
            }

            orderEntity.setOrderItemEntities(orderItems);

            redeemLoyaltyTransaction(orderRequest, userEntity, orderEntity);

            if (orderRequest.getPaymentMethod().equalsIgnoreCase(Constants.COD)) {
                PaymentEntity paymentEntity = mapToPaymentEntity(orderRequest, orderEntity);
                paymentRepository.save(paymentEntity);

                orderEntity.setOrderStatus(OrderStatus.CONFIRMED);
                for (OrderItemEntity orderItemEntity : orderEntity.getOrderItemEntities()) {
                    orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
                    orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
                }
                orderRepository.save(orderEntity);
                paymentServiceImpl.sendOrderConfirmation(userEntity, orderEntity);
                log.info("Whatsapp notification sent successfully for order confirmation");

            }

            if (!isNull(orderRequest.getOrderItemRequests()) && !orderRequest.getOrderItemRequests().isEmpty()) {
                orderRequest.getOrderItemRequests().forEach(this::decreaseProductQuantity);
                updateProductCache(orderRequest);
            }

            orderItems.stream()
                    .map(OrderItemEntity::getOrderItemId)
                    .forEach(orderItemId -> loyaltyPointsService.processOrderLoyaltyPoints(orderItemId, orderRequest.getUserId()));

            removerCartItemsFromTheDB(orderRequest);

            PlaceAnOrderResponse placeAnOrderResponse = new PlaceAnOrderResponse();
            placeAnOrderResponse.setMessage(Constants.ORDER_PLACED_SUCCESSFULLY);
            placeAnOrderResponse.setOrderId(orderEntity.getOrderId());

            log.info("Place an order Response: {}", placeAnOrderResponse);
            return placeAnOrderResponse;
        } catch (UserNotFoundException | AddressNotFoundException e) {
            log.error(Constants.EXCEPTION, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImplementation, PlaceAnOrder, Exception: {}", e.getMessage());
            throw new OrderPlacingException(e.getMessage());
        }
    }

    private void removerCartItemsFromTheDB(OrderRequest orderRequest) {
        SuccessResponse response = removeItemsFromCart(orderRequest.getUserId(), orderRequest.getOrderItemRequests());
        log.info("Removed items from cart: {}", response);
    }

    @Transactional
    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        log.info("Cancel Order Request : {}", request);
        try {
            OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(request.getOrderId(), request.getUserId());
            if (isNull(orderEntity)) {
                throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
            }

            validateOrderForCancellation(orderEntity, request.getOrderItemId());

            OrderItemEntity orderItem = orderServiceMappingHelper.getOrderItemEntity(orderEntity, request.getOrderItemId());
            orderItem.setOrderStatus(OrderStatus.CANCELED);
            orderItem.setDeliveryStatus(DeliveryStatus.CANCELLED);
            orderItem.setCancellationReason(request.getReason());
            orderItem.setCancelDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
            orderRepository.save(orderEntity);

            whatsAppService.notifyOrderCancellation(orderEntity.getUserEntity().getPhoneNumber(), orderEntity.getUserEntity().getFirstName(), orderItem.getOrderItemId());
            log.info("Whatsapp notification sent successfully for order cancellation");

            if (orderEntity.getPaymentEntity().getPaymentMethod().equalsIgnoreCase(Constants.COD)) {
                return createCancelOrderResponse(orderEntity, Constants.REFUND_NOT_APPLICABLE);
            } else {
                String refundMessage = handlePaymentAndRefund(orderItem, request);
                return createCancelOrderResponse(orderEntity, refundMessage);
            }

        } catch (OrderNotFoundException | OrderCancellationException | OrderItemNotFoundException e) {
            log.error("OrderServiceImplementation, cancelOrder, Order Details Exception: {}", e.getMessage());
            throw e;
        } catch (RefundProcessException e) {
            log.error("OrderServiceImplementation, cancelOrder, Refund DetailsException: {}", e.getMessage());
            throw e;
        } catch (PaymentNotFoundException e) {
            log.error("OrderServiceImplementation, cancelOrder, Payment Details Exception: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public ReturnOrderResponse returnOrder(ReturnRequest returnRequest) {
        log.info("Return Order Request : {}", returnRequest);
        OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(returnRequest.getOrderId(), returnRequest.getUserId());
        if (isNull(orderEntity)) {
            throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
        }

        validateReturnEligibilityAndOrderEligibility(orderEntity, returnRequest.getOrderItemId());

        ReturnOrderResponse response = new ReturnOrderResponse();

        if (REFUND.equals(returnRequest.getReturnType())) {
            OrderRefundResponse orderRefundResponse = handleRefundProcess(orderEntity, returnRequest);
            response.setOrderRefundResponse(orderRefundResponse);
        } else if (EXCHANGE.equals(returnRequest.getReturnType())) {
            OrderExchangeResponse orderExchangeResponse = handleExchangeProcess(orderEntity, returnRequest);
            response.setOrderExchangeResponse(orderExchangeResponse);
        }

        log.info("Return Order Response : {}", response);
        return response;
    }


    @Override
    public GetAllOrdersResponse getAllOrders(String userId) {
        log.info("Get All Orders Request userId: {}", userId);
        Optional<List<OrderEntity>> orderEntitiesOptional = orderRepository.findByUserEntityUserId(userId);
        log.info("List of Order Entities: {}", orderEntitiesOptional);

        if (orderEntitiesOptional.isEmpty()) {
            return new GetAllOrdersResponse(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        List<OrderEntity> orderEntities = new ArrayList<>(orderEntitiesOptional.get());
        orderEntities.sort(Comparator.comparing(OrderEntity::getOrderDate).reversed());

        List<GetOrdersResponse> recentOrders = new ArrayList<>();
        List<GetOrdersResponse> buyAgain = new ArrayList<>();
        List<GetOrdersResponse> cancelledOrders = new ArrayList<>();

        for (OrderEntity orderEntity : orderEntities) {
            GetOrdersResponse getOrdersResponse = mapToGetOrdersResponse(orderEntity);
            recentOrders.add(getOrdersResponse);

            if (isEligibleForBuyAgain(orderEntity)) {
                buyAgain.add(getOrdersResponse);
            }

            if (isCancelledOrder(orderEntity)) {
                cancelledOrders.add(getOrdersResponse);
            }
        }

        log.info("GetOrdersResponse (recentOrders): {}", recentOrders);
        log.info("GetOrdersResponse (buyAgain): {}", buyAgain);
        log.info("GetOrdersResponse (cancelledOrders): {}", cancelledOrders);

        return new GetAllOrdersResponse(recentOrders, buyAgain, cancelledOrders);
    }

    @Override
    public OrderSummaryResponse getOrderDetailsByOrderId(String orderId, String userId) {

        log.info("Get Order Details Request for orderId: {}", orderId);
        Optional<OrderEntity> orderEntityOptional = orderRepository.findById(orderId);

        if (orderEntityOptional.isEmpty()) {
            log.warn("No order found for orderId: {}", orderId);
            throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND + orderId);
        }

        OrderEntity orderEntity = orderEntityOptional.get();

        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setOrderId(orderEntity.getOrderId());
        response.setOrderDate(orderEntity.getOrderDate());
        response.setOrderStatus(orderEntity.getOrderStatus());

        List<OrderItemResponse> orderItemResponses = orderEntity.getOrderItemEntities()
                .stream()
                .map(orderServiceMappingHelper::mapToOrderItemResponse)
                .toList();

        response.setOrderItemResponse(orderItemResponses);
        response.setAddressResponse(mapToAddressResponse(orderEntity.getAddressEntity()));
        response.setPaymentResponse(mapToPaymentResponse(orderEntity.getPaymentEntity()));

        return response;
    }

    @Override
    public SuccessResponse checkReturnedProduct(CheckReturnedProductRequest request) {
        log.info("Check Returned Product Request : {}", request);

        OrderEntity orderEntity = orderRepository.findByOrderId(request.getOrderId());
        log.info("Order Entity Data : {}", orderEntity);
        if (isNull(orderEntity)) {
            log.error(Constants.ORDER_NOT_FOUND + Constants.BRACKETS + request.getOrderId());
            throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
        }

        OrderItemEntity orderItemEntity = orderEntity.getOrderItemEntities()
                .stream()
                .filter(item -> item.getOrderItemId().equals(request.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error(Constants.ORDER_ITEM_NOT_FOUND + Constants.BRACKETS + request.getOrderItemId());
                    return new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND);
                });

        if (!orderItemEntity.getQuantity().equals(request.getReturnedQuantity())) {
            log.error(Constants.INVALID_RETURNED_QUANTITY + Constants.BRACKETS + request.getReturnedQuantity());
            throw new InvalidQuantityException(Constants.INVALID_RETURNED_QUANTITY);
        }

        if (ReturnStatus.RETURN_COMPLETED.equals(orderItemEntity.getReturnStatus())) {
            log.error("Product already returned for Order Item ID: {}", request.getOrderItemId());
            return new SuccessResponse(Constants.RETURN_ALREADY_CONFIRMED, HttpStatus.OK.value());
        }

        if (ReturnStatus.RETURN_REQUESTED.equals(orderItemEntity.getReturnStatus())) {
            orderItemEntity.setReturnStatus(ReturnStatus.RETURN_COMPLETED);
            orderItemRepository.save(orderItemEntity);

            SuccessResponse response = new SuccessResponse();
            response.setMessage(Constants.RETURN_CONFIRMED_SUCCESS);
            log.info("Product return confirmed for Order Item ID: {}", request.getOrderItemId());

            return response;
        }

        log.error("Invalid return status for Order Item ID: {}", request.getOrderItemId());
        throw new InvalidReturnOperationException(Constants.INVALID_RETURN_STATUS);
    }

    @Transactional
    @Override
    public SuccessResponse revertPlaceAnOrder(String orderId, String userId) {
        try {
            log.info("Revert Place An Order OrderId: {}, UserId: {}", orderId, userId);
            OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(orderId, userId);
            if (isNull(orderEntity)) {
                log.error(Constants.ORDER_NOT_FOUND + Constants.BRACKETS + orderId);
                throw new OrderNotFoundException(Constants.ORDER_NOT_FOUND);
            }
            restoreStock(orderEntity);
            updateRestoreStockInCache(orderEntity);
            updateLoyaltyTransaction(orderEntity);

            orderRepository.delete(orderEntity);
            SuccessResponse response = new SuccessResponse();
            response.setMessage(Constants.PLACE_AN_ORDER_REVERTED_SUCCESS);
            response.setStatusCode(HttpStatus.OK.value());
            log.info("Revert Place An Order Response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error in Revert Place An Order: {}", e.getMessage());
            throw new OrderPlacingException(Constants.PLACE_AN_ORDER_REVERT_ERR_MSG);
        }
    }

    private void redeemLoyaltyTransaction(OrderRequest orderRequest, UserEntity userEntity, OrderEntity orderEntity) {
        if (!isNull(orderRequest.getLoyaltyPointsToRedeem()) && orderRequest.getLoyaltyPointsToRedeem() != 0.0) {
            SuccessResponse successResponse = loyaltyPointsService.redeemLoyaltyPoints(userEntity.getUserId(), orderEntity, orderRequest.getLoyaltyPointsToRedeem());
            log.info("Loyalty points redeemed: {}", successResponse);
        }
    }

    private void updateProductCache(OrderRequest orderRequest) {
        Cache cache = cacheManager.getCache(PRODUCTS_CACHE_NAME);
        if (!isNull(cache)) {
            ProductResponse cachedProductResponse = cache.get(PRODUCT_CACHE_KEY, ProductResponse.class);
            if (!isNull(cachedProductResponse)) {

                ProductResponse updatedProductResponse = updateProductQuantityInCache(cachedProductResponse, orderRequest.getOrderItemRequests());
                cache.put(PRODUCT_CACHE_KEY, updatedProductResponse);

                log.info("Updated 'listAllProduct' cache after placing an order: {}", updatedProductResponse);
            }
        }
    }

    public PaymentEntity mapToPaymentEntity(OrderRequest orderRequest, OrderEntity orderEntity) {
        PaymentEntity paymentEntity = new PaymentEntity();

        Double orderTotalAmount = orderRequest.getTotalAmount() + (isNull(orderRequest.getLoyaltyPointsToRedeem()) ? 0.0 : orderRequest.getLoyaltyPointsToRedeem());
        // payment id is for us reference
        paymentEntity.setPaymentId(generator.generateId(Constants.PAYMENT_ID));
        paymentEntity.setTotalAmount(orderTotalAmount);
        paymentEntity.setUserId(orderRequest.getUserId());
        paymentEntity.setPaymentMethod(orderRequest.getPaymentMethod());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setRedeemedLoyaltyPoints(isNull(orderRequest.getLoyaltyPointsToRedeem()) ? 0.0 : orderRequest.getLoyaltyPointsToRedeem());
        paymentEntity.setOrderSubTotal(orderRequest.getTotalAmount());
        paymentEntity.setOrderEntity(orderEntity);

        return paymentEntity;
    }

    private ProductResponse updateProductQuantityInCache(ProductResponse cachedProductResponse, List<OrderItemRequest> orderItemRequests) {

        orderItemRequests.forEach(itemRequest -> {
            String productId = itemRequest.getProductId();
            int quantityOrdered = itemRequest.getQuantity();

            cachedProductResponse.getCategoryResponses().stream()
                    .flatMap(category -> category.getSubCategoryResponses().stream())
                    .flatMap(subCategory -> subCategory.getProductDataResponses().stream())
                    .filter(product -> product.getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(product ->
                            product.getStockQuantityResponseList().forEach(stock ->
                                    stock.getColorQuantityResponses().forEach(colorQuantity -> {
                                        if (colorQuantity.getQuantity() >= quantityOrdered) {
                                            colorQuantity.setQuantity(colorQuantity.getQuantity() - quantityOrdered);
                                        } else {
                                            log.warn("Insufficient stock for product {} (color: {}, size: {}).",
                                                    productId, colorQuantity.getColor(), stock.getSize());
                                        }
                                    })
                            )
                    );
        });
        return cachedProductResponse;
    }

    private void decreaseProductQuantity(OrderItemRequest orderItemRequest) {
        Optional<Product> optionalProduct = productRepository.findById(orderItemRequest.getProductId());
        log.info("Product : {}", optionalProduct);

        Product product = optionalProduct.orElseThrow(() ->
                new ProductNotFoundException(Constants.PRODUCT_NOT_FOUND + orderItemRequest.getProductId()));

        StockQuantity stockQuantity = product.getStockQuantities().stream()
                .filter(stock -> stock.getSize().equals(orderItemRequest.getSize()))
                .findFirst()
                .orElseThrow(() -> new StockNotFoundException(Constants.STOCK_NOT_FOUND + orderItemRequest.getSize()));

        ColorQuantity colorQuantity = stockQuantity.getColorQuantities().stream()
                .filter(color -> color.getColor().equalsIgnoreCase(orderItemRequest.getColor()))
                .findFirst()
                .orElseThrow(() -> new ColorNotFoundException(Constants.COLOR_NOT_FOUND + orderItemRequest.getColor()));

        if (colorQuantity.getQuantity() < orderItemRequest.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for color: " + orderItemRequest.getColor() +
                    ", size: " + orderItemRequest.getSize() + ", product: " + product.getProductName());
        }

        colorQuantity.setQuantity(colorQuantity.getQuantity() - orderItemRequest.getQuantity());

        colorQuantityRepository.save(colorQuantity);
        log.info("Decreased stock quantity for product {}, size {}, color {}", product.getProductId(), stockQuantity.getSize(), colorQuantity.getColor());
    }

    private boolean isEligibleForBuyAgain(OrderEntity orderEntity) {
        OrderItemEntity orderItemEntity = orderEntity.getOrderItemEntities().getFirst();
        if (orderItemEntity.getDeliveryStatus() == null ||
                !DeliveryStatus.DELIVERED.equals(orderItemEntity.getDeliveryStatus())) {
            return false;
        }

        LocalDateTime deliveryDate = orderItemEntity.getDeliveryDate();
        if (isNull(deliveryDate)) {
            return false;
        }
        for (OrderItemEntity orderItem : orderEntity.getOrderItemEntities()) {
            Integer returnDaysPolicy = orderItem.getReturnDaysPolicy();
            if (returnDaysPolicy != null) {
                long daysSinceDelivery = ChronoUnit.DAYS.between(deliveryDate, LocalDateTime.now(ZoneId.of(Constants.ZONE)));
                if (daysSinceDelivery > returnDaysPolicy) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCancelledOrder(OrderEntity orderEntity) {
        return OrderStatus.CANCELED.equals(orderEntity.getOrderStatus());
    }


    private GetOrdersResponse mapToGetOrdersResponse(OrderEntity orderEntity) {
        GetOrdersResponse response = new GetOrdersResponse();
        response.setOrderId(orderEntity.getOrderId());
        response.setOrderDate(orderEntity.getOrderDate());
        response.setOrderStatus(orderEntity.getOrderStatus());
        List<OrderItemResponse> orderItemResponses = orderEntity.getOrderItemEntities()
                .stream()
                .map(orderServiceMappingHelper::mapToOrderItemResponse)
                .toList();
        response.setOrderItemResponse(orderItemResponses);
        response.setPaymentResponse(mapToPaymentResponse(orderEntity.getPaymentEntity()));
        return response;
    }

    private AddressResponse mapToAddressResponse(AddressEntity addressEntity) {

        Optional<AddressEntity> optionalAddressEntity = addressRepository.findById(addressEntity.getAddressId());
        if (optionalAddressEntity.isEmpty()) {
            throw new AddressNotFoundException(Constants.ADDRESS_NOT_FOUND);
        }
        AddressEntity address = optionalAddressEntity.get();

        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setAddressId(address.getAddressId());
        addressResponse.setFirstName(address.getFirstName());
        addressResponse.setLastName(address.getLastName());
        addressResponse.setPhoneNumber(address.getPhoneNumber());
        addressResponse.setBuildingName(address.getBuildingName());
        addressResponse.setStreetName(address.getStreetName());
        addressResponse.setCityName(address.getCityName());
        addressResponse.setStateName(address.getStateName());
        addressResponse.setCountryName(address.getCountryName());
        addressResponse.setPinCode(address.getPinCode());
        addressResponse.setTypeOfAddress(address.getTypeOfAddress());
        addressResponse.setLandMark(address.getLandMark());
        return addressResponse;
    }

    private PaymentResponse mapToPaymentResponse(PaymentEntity paymentEntity) {

        Optional<PaymentEntity> optionalPaymentEntity = paymentRepository.findById(paymentEntity.getPaymentId());
        if (optionalPaymentEntity.isEmpty()) {
            throw new PaymentNotFoundException(Constants.PAYMENT_NOT_FOUND);
        }

        PaymentEntity payment = optionalPaymentEntity.get();

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(payment.getPaymentId());
        paymentResponse.setPaymentStatus(payment.getPaymentStatus());
        paymentResponse.setOrderSubTotal(payment.getOrderSubTotal());
        paymentResponse.setRedemptionPoints(payment.getRedeemedLoyaltyPoints());
        paymentResponse.setTotalAmount(payment.getTotalAmount());
        paymentResponse.setPaymentDate(payment.getPaymentDate());
        paymentResponse.setPaymentMethod(payment.getPaymentMethod());
        return paymentResponse;
    }

    private void validateReturnEligibilityAndOrderEligibility(OrderEntity orderEntity, String orderItemId) {
        OrderItemEntity orderItemEntity = orderServiceMappingHelper.getOrderItemEntity(orderEntity, orderItemId);

        if (OrderStatus.RETURNED.equals(orderEntity.getOrderStatus())) {
            throw new ReturnException(Constants.ORDER_ALREADY_RETURNED);
        }

        if (!OrderStatus.DELIVERED.equals(orderEntity.getOrderStatus()) ||
                !DeliveryStatus.DELIVERED.equals(orderItemEntity.getDeliveryStatus())) {
            throw new InvalidReturnOperationException(Constants.ORDER_NOT_ELIGIBLE_FOR_RETURN);
        }

        if (ReturnStatus.REFUND_COMPLETED.equals(orderItemEntity.getReturnStatus())) {
            throw new InvalidReturnOperationException(Constants.ORDER_ITEM_ALREADY_REFUNDED);
        }

        if (ReturnStatus.EXCHANGED_COMPLETED.equals(orderItemEntity.getReturnStatus())) {
            throw new InvalidReturnOperationException(Constants.ALREADY_EXCHANGED);
        }

        if (ChronoUnit.DAYS.between(orderItemEntity.getDeliveryDate(), LocalDateTime.now(ZoneId.of(Constants.ZONE))) > 7) {
            throw new ReturnException(Constants.RETURN_PERIOD_EXPIRED);
        }

        if (ReturnStatus.EXCHANGE.equals(orderItemEntity.getReturnStatus())) {
            throw new InvalidReturnOperationException(Constants.CANNOT_EXCHANGE_MULTIPLE_ITEMS);
        }
    }

    private void validateOrderForCancellation(OrderEntity orderEntity, String orderItemId) {
        OrderItemEntity orderItem = orderServiceMappingHelper.getOrderItemEntity(orderEntity, orderItemId);

        if (OrderStatus.CANCELED.equals(orderItem.getOrderStatus())
                || DeliveryStatus.DELIVERED.equals(orderItem.getDeliveryStatus())
                || DeliveryStatus.SHIPPED.equals(orderItem.getDeliveryStatus())
                || DeliveryStatus.RETURNED.equals(orderItem.getDeliveryStatus())) {
            throw new OrderCancellationException(Constants.ORDER_CANNOT_BE_CANCELED);
        }

        long daysBetween = ChronoUnit.DAYS.between(orderEntity.getOrderDate(), LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        if (daysBetween > 7) {
            throw new OrderCancellationException(Constants.ORDER_CANNOT_BE_CANCELED_AFTER_7_DAYS);
        }
    }

    private String handlePaymentAndRefund(OrderItemEntity orderItemEntity, CancelOrderRequest request) {
        Optional<PaymentEntity> payment = getPaymentEntityData(request.getOrderId());
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException(Constants.PAYMENT_NOT_FOUND);
        }

        if (!PaymentStatus.SUCCESS.equals(payment.get().getPaymentStatus())) {
            throw new RefundProcessException(Constants.PAYMENT_NOT_SUCCESSFUL);
        }

        if (Boolean.TRUE.equals(request.getIsRefund())) {
            return processRefund(payment.get(), orderItemEntity, request.getReason());
        }

        return Constants.NO_SPECIFIC_ACTION_REQUESTED;
    }

    private String processRefund(PaymentEntity payment, OrderItemEntity orderItemEntity, String reason) {
        RefundEntity refundEntity = refundRepository.findByPaymentEntityAndOrderItemEntity(payment, orderItemEntity);
        if (isNull(refundEntity)) {
            refundEntity = new RefundEntity();
            refundEntity.setRefundId(generator.generateId(Constants.REFUND_ID));
            refundEntity.setRefundStatus(RefundStatus.NOT_REQUESTED);
            refundEntity.setPaymentEntity(payment);
            refundEntity.setOrderItemEntity(orderItemEntity);
            refundEntity.setReason(reason);
            refundRepository.save(refundEntity);
        }

        RefundStatus refundStatus = refundEntity.getRefundStatus();

        switch (refundStatus) {

            case NOT_REQUESTED:
                refundEntity.setRefundStatus(RefundStatus.REQUESTED);
                refundRepository.save(refundEntity);
                return initiateRefund(payment, orderItemEntity).getStatusCode() == 200
                        ? Constants.REFUND_REQUEST_INITIATED
                        : Constants.REFUND_FAILED;

            case INITIATED:
                throw new RefundProcessException(Constants.REFUND_ALREADY_INITIATED);

            case REQUESTED:
                throw new RefundProcessException(Constants.REFUND_ALREADY_REQUESTED);

            case COMPLETED:
                throw new RefundProcessException(Constants.REFUND_ALREADY_COMPLETED);

            case FAILED:
                throw new RefundProcessException(Constants.REFUND_PROCESS_FAILED);

            default:
                throw new RefundProcessException(Constants.UNKNOWN_REFUND_STATUS);
        }
    }

    private CancelOrderResponse createCancelOrderResponse(OrderEntity orderEntity, String refundMessage) {
        CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
        cancelOrderResponse.setOrderDate(orderEntity.getOrderDate());
        cancelOrderResponse.setCancelledDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        cancelOrderResponse.setOrderStatus(OrderStatus.CANCELED);
        cancelOrderResponse.setDeliveryStatus(DeliveryStatus.CANCELLED);
        cancelOrderResponse.setMessage(Constants.ORDER_CANCELED_SUCCESSFULLY);
        cancelOrderResponse.setOrderId(orderEntity.getOrderId());
        cancelOrderResponse.setRefundMessage(refundMessage);
        return cancelOrderResponse;
    }


    private SuccessResponse initiateRefund(PaymentEntity paymentEntity, OrderItemEntity orderItemEntity) {
        try {
            SuccessResponse successResponse = paymentServiceImpl.refundProcess(paymentEntity, orderItemEntity);
            log.info("Refund processed successfully: {}", successResponse);
            return successResponse;
        } catch (RefundProcessException e) {
            log.error("Refund initiation failed for order ID: {}, reason: {}", paymentEntity.getOrderEntity().getOrderId(), e.getMessage());
            throw e;
        }
    }

    private List<OrderItemEntity> getNewOrderItemEntities(List<OrderItemRequest> orderItemRequests, OrderEntity
            orderEntity) {

        if (isNull(orderItemRequests) || orderItemRequests.isEmpty()) {
            return Collections.emptyList();
        }

        return orderItemRequests.stream()
                .map(itemRequest -> {
                    OrderItemEntity orderItemEntity = new OrderItemEntity();
                    orderItemEntity.setOrderItemId(generator.generateId(Constants.ORDER_ITEM_ID));
                    orderItemEntity.setProductId(itemRequest.getProductId());
                    orderItemEntity.setQuantity(itemRequest.getQuantity());
                    orderItemEntity.setUnitPrice(itemRequest.getTotalAmount() / itemRequest.getQuantity());
                    orderItemEntity.setTotalAmount(itemRequest.getTotalAmount());
                    orderItemEntity.setProductColor(itemRequest.getColor());
                    orderItemEntity.setProductSize(itemRequest.getSize());
                    orderItemEntity.setProductOfferPercentage(itemRequest.getOfferPercentage());
                    orderItemEntity.setOrderEntity(orderEntity);
                    orderItemEntity.setReturnDaysPolicy(Constants.RETURN_DAYS_POLICY);
                    orderItemEntity.setRedeemedLoyaltyPoints(0.0);
                    orderItemEntity.setOrderStatus(OrderStatus.PENDING);
                    orderItemEntity.setDeliveryStatus(DeliveryStatus.PENDING);
                    orderItemEntity.setReturnStatus(ReturnStatus.NOT_RETURNED);
                    return orderItemEntity;
                })
                .toList();
    }

    private OrderEntity getNewOrderEntity(OrderRequest orderRequest, UserEntity userEntity) {
        Double orderTotalAmount = orderRequest.getTotalAmount() + (isNull(orderRequest.getLoyaltyPointsToRedeem()) ? 0.0 : orderRequest.getLoyaltyPointsToRedeem());
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(generator.generateId(Constants.ORDER_ID));
        orderEntity.setOrderDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        orderEntity.setTotalAmount(orderTotalAmount);
        orderEntity.setOrderSubTotal(orderRequest.getTotalAmount());
        orderEntity.setRedeemedLoyaltyPoints(isNull(orderRequest.getLoyaltyPointsToRedeem()) ? 0.0 : orderRequest.getLoyaltyPointsToRedeem());
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        orderEntity.setPaymentMethod(orderRequest.getPaymentMethod());
        orderEntity.setUserEntity(userEntity);
        orderEntity.setAddressEntity(mapAddressEntity(orderRequest.getUserId(), orderRequest.getShippingAddress()));
        return orderEntity;
    }

    private AddressEntity mapAddressEntity(String userId, AddressRequest shippingAddress) {
        log.info("Shipping Address Request: {}", shippingAddress);
        AddressEntity addressEntity = addressRepository.findByUserEntityUserIdAndAddressId(userId, shippingAddress.getAddressId());
        if (!isNull(addressEntity)) {
            return addressEntity;
        } else {
            throw new AddressNotFoundException(Constants.ADDRESS_NOT_FOUND);
        }
    }

    private OrderExchangeResponse handleExchangeProcess(OrderEntity orderEntity, ReturnRequest returnRequest) {
        OrderItemRequest request = returnRequest.getReplacementOrderItemRequest();

        OrderItemEntity originalItem = orderServiceMappingHelper.getOrderItemEntity(orderEntity, returnRequest.getOrderItemId());

        double priceDifference = request.getTotalAmount() - originalItem.getTotalAmount();

        if (priceDifference > 0) {
            return new OrderExchangeResponse(true, priceDifference, orderEntity.getOrderId(), Constants.ADDITIONAL_PAYMENT_REQUIRED);
        }

        OrderItemEntity entity = new OrderItemEntity();
        entity.setOrderEntity(orderEntity);
        entity.setOrderItemId(generator.generateId(Constants.ORDER_ITEM_ID));
        entity.setProductId(request.getProductId());
        entity.setProductColor(request.getColor());
        entity.setProductSize(request.getSize());
        entity.setQuantity(request.getQuantity());
        entity.setTotalAmount(request.getTotalAmount());
        entity.setProductOfferPercentage(request.getOfferPercentage());
        entity.setUnitPrice(request.getTotalAmount() / request.getQuantity());
        entity.setReturnDaysPolicy(Constants.RETURN_DAYS_POLICY);
        entity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        entity.setOrderStatus(OrderStatus.CONFIRMED);

        ReturnEntity returnEntity = createReturnEntity(returnRequest);

        entity.setReturnStatus(ReturnStatus.EXCHANGE_REQUESTED);
        entity.setReturnReplacementOrderItemId(returnEntity.getReturnId());
        orderItemRepository.save(entity);
        SuccessResponse response = loyaltyPointsService.processOrderLoyaltyPoints(entity.getOrderItemId(), orderEntity.getUserEntity().getUserId());
        log.info("Loyalty Points rewarded: {}", response);

        originalItem.setReturnStatus(ReturnStatus.EXCHANGE);
        originalItem.setReturnReason(returnEntity.getReturnReason());
        orderItemRepository.save(originalItem);

        if (!isNull(originalItem.getRedeemedLoyaltyPoints()) && originalItem.getRedeemedLoyaltyPoints() > 0) {
            SuccessResponse successResponse = loyaltyPointsService.refundPointsForCancelledOrderItem(orderEntity.getUserEntity().getUserId(), orderEntity.getOrderId(), originalItem.getOrderItemId());
            log.info("Cancelled loyalty points for order item: {}", successResponse);
        }

        return new OrderExchangeResponse(false, priceDifference, orderEntity.getOrderId(), Constants.EXCHANGE_SUCCESSFUL);
    }

    private OrderRefundResponse handleRefundProcess(OrderEntity orderEntity, ReturnRequest returnRequest) {
        OrderItemEntity item = orderServiceMappingHelper.getOrderItemEntity(orderEntity, returnRequest.getOrderItemId());

        if (ReturnStatus.REFUND_COMPLETED.equals(item.getReturnStatus())) {
            throw new InvalidReturnOperationException(Constants.ORDER_ITEM_ALREADY_REFUNDED);
        }

        ReturnEntity entity = createReturnEntity(returnRequest);

        item.setReturnStatus(ReturnStatus.RETURN_REQUESTED);
        item.setReturnDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        item.setReturnReplacementOrderItemId(entity.getReturnId());
        item.setUserBankId(returnRequest.getUserBankId());
        orderItemRepository.save(item);

        OrderRefundResponse orderRefundResponse = new OrderRefundResponse();
        orderRefundResponse.setRefundAmount(item.getTotalAmount());
        orderRefundResponse.setMessage(Constants.PRODUCT_RETURN_PENDING_MSG);

        return orderRefundResponse;
    }

    private ReturnEntity createReturnEntity(ReturnRequest returnRequest) {
        ReturnEntity returnEntity = new ReturnEntity();
        returnEntity.setReturnId(generator.generateId(Constants.RETURN_ID));
        returnEntity.setOrderId(returnRequest.getOrderId());
        returnEntity.setOrderItemId(returnRequest.getOrderItemId());
        returnEntity.setUserId(returnRequest.getUserId());
        returnEntity.setReturnDate(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        returnEntity.setReturnReason(returnRequest.getReason());
        returnEntity.setReturnStatus(ReturnStatus.REFUND_INITIATED);
        returnEntity.setReturnType(returnRequest.getReturnType());

        returnRepository.save(returnEntity);

        return returnEntity;
    }

    private Optional<PaymentEntity> getPaymentEntityData(String orderId) {
        return paymentRepository.findByOrderEntityOrderId(orderId);
    }

    private SuccessResponse removeItemsFromCart(String userId, List<OrderItemRequest> orderItemRequests) {
        if (isNull(orderItemRequests) || orderItemRequests.isEmpty()) {
            return new SuccessResponse(Constants.NO_CART_ITEMS_REMOVED, HttpStatus.OK.value());
        }

        boolean itemsRemoved = false;

        try {
            for (OrderItemRequest orderItem : orderItemRequests) {
                long deletedCount = cartRepository.deleteItemsByDetails(
                        mongoTemplate,
                        userId,
                        orderItem.getProductId(),
                        orderItem.getSize(),
                        orderItem.getColor(),
                        orderItem.getQuantity()
                );

                if (deletedCount > 0) {
                    itemsRemoved = true;
                }
            }

            if (!itemsRemoved) {
                return new SuccessResponse(Constants.NO_MATCHING_CART_ITEMS, HttpStatus.OK.value());
            }

            return new SuccessResponse(Constants.CART_ITEM_DELETED, HttpStatus.OK.value());
        } catch (Exception ex) {
            log.error("Error occurred while removing cart items for user: {}, Error: {}", userId, ex.getMessage(), ex);
            return new SuccessResponse(Constants.CART_ITEM_REMOVAL_FAILED, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void restoreStock(OrderEntity orderEntity) {
        for (OrderItemEntity orderItem : orderEntity.getOrderItemEntities()) {
            Product product = productRepository.findByProductId(orderItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(Constants.PRODUCT_NOT_FOUND + Constants.BRACKETS + orderItem.getProductId()));

            updateStockQuantities(product, orderItem);
            productRepository.save(product);
        }
    }

    private void updateStockQuantities(Product product, OrderItemEntity orderItem) {
        product.getStockQuantities().stream()
                .filter(stock -> stock.getSize().equals(orderItem.getProductSize()))
                .forEach(stock -> stock.getColorQuantities().stream()
                        .filter(colorStock -> colorStock.getColor().equals(orderItem.getProductColor()))
                        .forEach(colorStock -> colorStock.setQuantity(colorStock.getQuantity() + orderItem.getQuantity())));
    }

    private void updateRestoreStockInCache(OrderEntity orderEntity) {
        Cache cache = cacheManager.getCache(PRODUCTS_CACHE_NAME);
        if (!isNull(cache)) {
            ProductResponse cachedProductResponse = cache.get(PRODUCT_CACHE_KEY, ProductResponse.class);
            if (!isNull(cachedProductResponse)) {
                ProductResponse productResponse = updateInCache(cachedProductResponse, orderEntity);
                cache.put(PRODUCT_CACHE_KEY, productResponse);

                log.info("Updated 'listAllProduct' cache after revert an placing order: {}", productResponse);
            }
        }
    }

    private ProductResponse updateInCache(ProductResponse cachedProductResponse, OrderEntity orderEntity) {
        orderEntity.getOrderItemEntities().forEach(orderItem -> cachedProductResponse.getCategoryResponses().stream()
                .flatMap(category -> category.getSubCategoryResponses().stream())
                .flatMap(subCategory -> subCategory.getProductDataResponses().stream())
                .filter(product -> product.getProductId().equals(orderItem.getProductId()))
                .findFirst()
                .ifPresent(product ->
                        product.getStockQuantityResponseList().forEach(stock ->
                                stock.getColorQuantityResponses().forEach(colorQuantity -> {
                                    if (colorQuantity.getColor().equals(orderItem.getProductColor()) && stock.getSize().equals(orderItem.getProductSize())) {
                                        colorQuantity.setQuantity(colorQuantity.getQuantity() + orderItem.getQuantity());
                                    }
                                })
                        )
                ));
        return cachedProductResponse;
    }

    private void updateLoyaltyTransaction(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>(orderEntity.getOrderItemEntities());
        for (OrderItemEntity orderItem : orderItemEntities) {
            String orderItemId = orderItem.getOrderItemId();
            Double redeemPoints = orderItem.getRedeemedLoyaltyPoints();
            String userId = orderEntity.getUserEntity().getUserId();

            LoyaltyPointsEntity loyaltyPoints = loyaltyPointsRepository.findByUserEntityUserId(userId);
            List<LoyaltyPointsTransactionEntity> transactionEntities = loyaltyPoints.getLoyaltyPointsTransactionEntities();

            Iterator<LoyaltyPointsTransactionEntity> iterator = transactionEntities.iterator();
            List<LoyaltyPointsTransactionEntity> matchedEntity = new ArrayList<>();
            while (iterator.hasNext()) {
                LoyaltyPointsTransactionEntity entity = iterator.next();

                if (entity.getOrderItemId().equals(orderItemId)) {
                    matchedEntity.add(entity);
                    if (entity.getTransactionType().equals(LoyaltyTransactionType.REDEMPTION) &&
                            entity.getPointsChange().equals(redeemPoints)) {
                        loyaltyPoints.setTotalRedeemedPoints(loyaltyPoints.getTotalRedeemedPoints() - redeemPoints);
                        loyaltyPoints.setTotalLoyaltyPoints(loyaltyPoints.getTotalLoyaltyPoints() + redeemPoints);
                    }
                    iterator.remove();
                }
            }
            loyaltyPointsRepository.save(loyaltyPoints);
            loyaltyPointsTransactionRepository.deleteAll(matchedEntity);
        }
    }
}
