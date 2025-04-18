package com.cordestitch.entity.otp;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_data")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OTPEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_verified")
    private boolean otpVerified;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "email_address_otp_code")
    private String emailAddressOtpCode;

    @Column(name = "email_address_verified")
    private boolean emailAddressVerified;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "otp_created_at", nullable = false)
    private LocalDateTime otpCreatedAt;
}
