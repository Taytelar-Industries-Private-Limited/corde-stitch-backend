package com.cordestitch.repository.otp;

import com.cordestitch.entity.otp.OTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity,Long> {
    OTPEntity findByPhoneNumber(String phoneNumber);

    Optional<OTPEntity> findByEmailAddress(String emailAddress);
}
