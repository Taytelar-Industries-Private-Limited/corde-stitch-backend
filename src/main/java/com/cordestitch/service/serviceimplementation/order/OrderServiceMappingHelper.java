package com.cordestitch.service.serviceimplementation.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.customization.Fabric;
import com.cordestitch.entity.customization.UserCustomizationEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.ProductImage;
import com.cordestitch.exception.customization.ConvertFromJsonException;
import com.cordestitch.exception.customization.FabricNotFoundException;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.repository.customization.UserCustomizationRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import com.cordestitch.response.order.CustomizedCartItemResponse;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

import static java.util.Objects.isNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderServiceMappingHelper {

    private final ProductRepository productRepository;

    private final UserCustomizationRepository userCustomizationRepository;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final ReviewService reviewService;


    public CustomizedCartItemResponse mapToCustomizedCartItemResponse(OrderItemEntity orderItemEntity) {

        CustomizedCartItemResponse customizedCartItemResponse = new CustomizedCartItemResponse();
        customizedCartItemResponse.setOrderItemId(orderItemEntity.getOrderItemId());
        customizedCartItemResponse.setQuantity(orderItemEntity.getQuantity());
        customizedCartItemResponse.setPrice(orderItemEntity.getUnitPrice());
        customizedCartItemResponse.setTotalAmount(orderItemEntity.getTotalAmount());
        customizedCartItemResponse.setProductColor(orderItemEntity.getProductColor());
        customizedCartItemResponse.setProductSize(String.valueOf(orderItemEntity.getProductSize()));
        customizedCartItemResponse.setReturnDaysPolicy(orderItemEntity.getReturnDaysPolicy());
        customizedCartItemResponse.setDeliveryDate(orderItemEntity.getDeliveryDate());
        customizedCartItemResponse.setDeliveryStatus(orderItemEntity.getDeliveryStatus());
        customizedCartItemResponse.setCancelOrderDate(orderItemEntity.getCancelDate());
        customizedCartItemResponse.setPaymentMethod(orderItemEntity.getOrderEntity().getPaymentEntity().getPaymentMethod());
        customizedCartItemResponse.setPaymentStatus(orderItemEntity.getOrderEntity().getPaymentEntity().getPaymentStatus());

        Optional<UserCustomizationEntity> customization = userCustomizationRepository.findById(orderItemEntity.getUserCustomizationEntity().getUserCustomizationId());
        if (customization.isPresent()) {

            UserCustomizationEntity userCustomizationEntity = customization.get();

            Fabric fabricDetails = extractFabricDetails(userCustomizationEntity.getFabric());
            customizedCartItemResponse.setPrice(fabricDetails.getFabricPrice());
            customizedCartItemResponse.setProductName(fabricDetails.getFabricColor() + " " + userCustomizationEntity.getPantType());
            customizedCartItemResponse.setProductDescription(fabricDetails.getFabricDescription());
            customizedCartItemResponse.setProductOfferPercentage(fabricDetails.getProductOfferPercentage());
            fabricDetails.getImageUrl()
                    .stream()
                    .findFirst()
                    .ifPresent(customizedCartItemResponse::setProductImageUrl);


            if (!isNull(orderItemEntity.getUserCustomizationEntity())) {
                CustomizedAddDataResponse addDataResponse = modelMapper.map(orderItemEntity.getUserCustomizationEntity(), CustomizedAddDataResponse.class);
                customizedCartItemResponse.setCustomizedAddDataResponse(addDataResponse);
            }
        }
        return customizedCartItemResponse;
    }

    public OrderItemResponse mapToOrderItemResponse(OrderItemEntity orderItemEntity) {
        log.info("Order Item Entity : {}",orderItemEntity);
        OrderItemResponse itemResponse = new OrderItemResponse();

        Optional<Product> productOptional = productRepository.findByProductId(orderItemEntity.getProductId());
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            String productColor = orderItemEntity.getProductColor();

            String firstImageUrl = product.getProductImages().stream()
                    .filter(productImage -> productImage.getColorName().equalsIgnoreCase(productColor))
                    .sorted(Comparator.comparingInt(ProductImage::getImagePriority))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null);
            itemResponse.setProductName(product.getProductName());
            itemResponse.setProductImage(firstImageUrl);
            itemResponse.setProductOfferPercentage(product.getProductOfferPercentage());
            itemResponse.setProductDescription(product.getProductDescription());
        }

        itemResponse.setOrderItemId(orderItemEntity.getOrderItemId());
        itemResponse.setProductId(orderItemEntity.getProductId());
        itemResponse.setQuantity(orderItemEntity.getQuantity());
        itemResponse.setUnitPrice(orderItemEntity.getUnitPrice());
        itemResponse.setProductColor(orderItemEntity.getProductColor());
        itemResponse.setProductSize(String.valueOf(orderItemEntity.getProductSize()));
        itemResponse.setTotalAmount(orderItemEntity.getTotalAmount());
        itemResponse.setReturnDaysPolicy(orderItemEntity.getReturnDaysPolicy());
        itemResponse.setDeliveryDate(orderItemEntity.getDeliveryDate());
        itemResponse.setDeliveryStatus(orderItemEntity.getDeliveryStatus());
        itemResponse.setCancelOrderDate(orderItemEntity.getCancelDate());
        itemResponse.setProductOfferPercentage(orderItemEntity.getProductOfferPercentage());
        itemResponse.setOrderStatus(orderItemEntity.getOrderStatus());
        itemResponse.setReturnStatus(orderItemEntity.getReturnStatus());
        itemResponse.setReturnOrderDate(orderItemEntity.getReturnDate());
        itemResponse.setReviewResponse(reviewService.getReviewById(orderItemEntity.getProductId(), orderItemEntity.getOrderEntity().getOrderId(), orderItemEntity.getOrderEntity().getUserEntity().getUserId()));

        log.info("Order Item Response: {}", itemResponse);
        return itemResponse;
    }


    public Fabric extractFabricDetails(JsonNode fabricNode) {

        if (isNull(fabricNode)) {
            throw new FabricNotFoundException(Constants.FABRIC_PARSE_ERROR);
        }
        try {
            Fabric fabricDetails = objectMapper.treeToValue(fabricNode, Fabric.class);
            return new Fabric(fabricDetails.getFabricId(),
                    fabricDetails.getFabricColor(),
                    fabricDetails.getFabricColorCode(),
                    fabricDetails.getFabricDescription(),
                    fabricDetails.getFabricPrice(),
                    fabricDetails.getProductOfferPercentage(),
                    fabricDetails.getImageUrl()
            );
        } catch (ConvertFromJsonException | JsonProcessingException e) {
            throw new FabricNotFoundException(Constants.FABRIC_PARSE_ERROR);
        }
    }

    public OrderItemEntity getOrderItemEntity(OrderEntity orderEntity, String orderItemId) {
        return orderEntity.getOrderItemEntities().stream()
                .filter(i -> i.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));
    }
}