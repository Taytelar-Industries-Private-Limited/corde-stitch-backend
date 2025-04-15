package com.cordestitch.entity.order;

import com.cordestitch.entity.alteration.SlotEntity;
import com.cordestitch.entity.customization.UserCustomizationEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.enums.DeliveryStatus;
import com.cordestitch.enums.OrderStatus;
import com.cordestitch.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders_item_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemEntity {

    @Id
    @Column(name = "order_item_id",nullable = false, unique = true)
    private String orderItemId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "product_color", nullable = false)
    private String productColor;

    @Column(name = "product_size", nullable = false)
    private Integer productSize;

    @Column(name = "product_offer_percentage", nullable = false)
    private Double productOfferPercentage;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "return_days_policy", nullable = false)
    private Integer returnDaysPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "redeemed_loyaltyPoints")
    private Double redeemedLoyaltyPoints;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false)
    private ReturnStatus returnStatus;

    @Column(name = "return_replacement_id")
    private String returnReplacementOrderItemId;

    @Column(name = "return_reason")
    private String returnReason;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "user_bank_id")
    private String userBankId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    @ToString.Exclude
    private OrderEntity orderEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    @ToString.Exclude
    private SlotEntity slotEntity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_customization_id", referencedColumnName = "user_customization_id")
    @ToString.Exclude
    private UserCustomizationEntity userCustomizationEntity;

    @OneToMany(mappedBy = "orderItemEntity", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<RefundEntity> refundEntities;

}
