package com.cordestitch.entity.user;

import com.cordestitch.entity.customization.UserCustomizationEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_data")
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "email_address_verified")
    private boolean emailAddressVerified;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_number_verified")
    private boolean phoneNumberVerified;

    @Column(name = "referralCode")
    private String referralCode;

    @Column(name = "referred_referral_code")
    private String referredReferralCode;

    @Column(name = "is_referred")
    private boolean isReferred;

    @Column(name = "authentication_source")
    private String authenticationSource;

    @Column(name = "user_created_at")
    private LocalDateTime userCreatedAt;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<AddressEntity> addressEntityList;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<OrderEntity> orderEntities;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<CardEntity> cardEntities;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<UserCustomizationEntity> userCustomizationEntityList;


    @OneToOne(mappedBy = "userEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private LoyaltyPointsEntity loyaltyPointsEntity;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<UserBankAccountEntity> userBankAccountEntities;
}
