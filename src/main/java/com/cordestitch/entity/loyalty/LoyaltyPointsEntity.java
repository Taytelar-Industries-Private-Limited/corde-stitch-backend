package com.cordestitch.entity.loyalty;

import com.cordestitch.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Table(name = "loyalty_points_data")
public class LoyaltyPointsEntity {

    @Id
    @Column(name = "loyalty_id")
    private String loyaltyId;

    @Column(name = "total_points")
    private Double totalLoyaltyPoints;

    @Column(name = "total_expired_points")
    private Double totalExpiredPoints;

    @Column(name = "total_redeemed_points")
    private Double totalRedeemedPoints;

    @Column(name = "is_referred")
    private Boolean isReferred;

    @Column(name = "referred_referral_code")
    private String referredReferralCode;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude
    private UserEntity userEntity;

    @OneToMany(mappedBy = "loyaltyPointsEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<LoyaltyPointsTransactionEntity> loyaltyPointsTransactionEntities;

}