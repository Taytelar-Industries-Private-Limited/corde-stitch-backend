package com.cordestitch.repository.user;

import com.cordestitch.entity.affiliate.AffiliateUserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffiliateUserRepository extends MongoRepository<AffiliateUserEntity, String> {
    AffiliateUserEntity findUserByPhoneNumber(String phoneNumber);

    Optional<AffiliateUserEntity> findByReferralCode(String referralCode);

    AffiliateUserEntity findByEmailAddress(String emailAddress);

    Optional<AffiliateUserEntity> findByAffiliateUserId(String affiliateUserId);
}