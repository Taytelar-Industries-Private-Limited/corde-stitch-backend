package com.cordestitch.service.service.otp;

import com.cordestitch.request.otp.OTPRequest;
import com.cordestitch.request.otp.ValidateOTP;
import com.cordestitch.response.otp.OTPResponse;

public interface OTPService {
    OTPResponse verifyOtp(ValidateOTP validateOTP);

    OTPResponse generateOtp(OTPRequest otpRequest);

    OTPResponse generateWhatsAppOtp(OTPRequest otpRequest);
}
