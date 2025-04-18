package com.cordestitch.repository.user;

import com.cordestitch.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,String> {
    UserEntity findUserByUserId(String userId);

    UserEntity findUserByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByReferralCode(String referralCode);

    UserEntity findByEmailAddress(String emailAddress);

    Optional<UserEntity> findByUserIdAndPhoneNumber(String userId, String phoneNumber);

    Optional<UserEntity> findByUserIdAndEmailAddress(String userId, String emailAddress);

    Optional<UserEntity> findByUserIdAndIsReferredIsFalse(String userId);
}
