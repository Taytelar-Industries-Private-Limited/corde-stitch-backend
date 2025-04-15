package com.cordestitch.service.serviceimplementation.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.ProductImage;
import com.cordestitch.exception.order.OrderItemNotFoundException;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.response.order.OrderItemResponse;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderServiceMappingHelper {

    private final ProductRepository productRepository;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final ReviewService reviewService;

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

    public OrderItemEntity getOrderItemEntity(OrderEntity orderEntity, String orderItemId) {
        return orderEntity.getOrderItemEntities().stream()
                .filter(i -> i.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));
    }
}