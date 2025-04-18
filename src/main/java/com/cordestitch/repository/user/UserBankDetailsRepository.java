package com.cordestitch.repository.user;

import com.cordestitch.entity.user.UserBankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBankDetailsRepository extends JpaRepository<UserBankAccountEntity, String> {

    UserBankAccountEntity findByUserEntityUserIdAndUserBankId(String userId, String userBankId);

    List<UserBankAccountEntity> findByUserEntityUserIdAndIsBankAccountDeletedFalse(String userId);

    UserBankAccountEntity findByUserBankId(String userBankId);

    List<UserBankAccountEntity> findByUserEntityUserId(String userId);

    boolean existsByRazorpayContactIdAndIsBankAccountDeletedFalse(String razorpayContactId);
}
