package com.cordestitch.entity.affiliate;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "affiliate_user_data")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AffiliateUserEntity {

    @Id
    @Field(name = "affiliate_user_id")
    private String affiliateUserId;

    @Field(name = "first_name")
    private String firstName;

    @Field(name = "last_name")
    private String lastName;

    @Field(name = "gender")
    private String gender;

    @Field(name = "user_type")
    private String userType;

    @Field(name = "email_address")
    private String emailAddress;

    @Field(name = "email_address_verified")
    private boolean emailAddressVerified;

    @Field(name = "phone_number")
    private String phoneNumber;

    @Field(name = "phone_number_verified")
    private boolean phoneNumberVerified;

    @Field(name = "referral_code")
    private String referralCode;

    @Field(name = "referred_referral_code")
    private String referredReferralCode;

    @Field(name = "authentication_source")
    private String authenticationSource;

    @Field(name = "user_created_at")
    private LocalDateTime userCreatedAt;

    @Field(name = "source_entity")
    private List<SourceEntity> sourceEntities;

}