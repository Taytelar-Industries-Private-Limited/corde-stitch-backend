package com.cordestitch.entity.loyalty;

import com.cordestitch.enums.LoyaltyTransactionStatus;
import com.cordestitch.enums.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "loyalty_transaction_data")
public class LoyaltyPointsTransactionEntity {

    @Id
    @Column(name = "loyalty_transaction_id")
    private String loyaltyTransactionId;

    @Column(name = "points_change")
    private Double pointsChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private LoyaltyTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_points_status")
    private LoyaltyTransactionStatus loyaltyTransactionStatus;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "order_item_id")
    private String orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_id", referencedColumnName = "loyalty_id")
    @ToString.Exclude
    private LoyaltyPointsEntity loyaltyPointsEntity;

}