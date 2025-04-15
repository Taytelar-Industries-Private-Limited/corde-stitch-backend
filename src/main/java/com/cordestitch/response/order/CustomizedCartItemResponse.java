package com.cordestitch.response.order;

import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.PaymentStatus;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomizedCartItemResponse {

    private String orderItemId;

    private Integer quantity;

    private Double price;

    private Double totalAmount;

    private String productSize;

    private int returnDaysPolicy;

    private String productName;

    private String productDescription;

    private Double productOfferPercentage;

    private String productImageUrl;

    private String productColor;

    private DeliveryStatus deliveryStatus;

    private LocalDateTime deliveryDate;

    private LocalDateTime cancelOrderDate;

    private String paymentMethod;

    private PaymentStatus paymentStatus;

    private CustomizedAddDataResponse customizedAddDataResponse;
}