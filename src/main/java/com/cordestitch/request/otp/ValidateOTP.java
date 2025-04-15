package com.cordestitch.request.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ValidateOTP {

    @NotBlank(message = "Phone Number is mandatory")
    @Pattern(regexp = "\\d{10}", message = "Phone Number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Otp Password is mandatory")
    @Pattern(regexp = "\\d{6}", message = "OTP Password must be exactly 6 digits")
    private String otpPassword;

    @NotBlank(message = "User Type cannot be null or empty")
    @Pattern(regexp = "customer|affiliate|admin", message = "User Type must be either 'customer' or 'affiliate' or 'admin'")
    private String userType;
}
