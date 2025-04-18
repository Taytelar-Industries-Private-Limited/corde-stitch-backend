package com.cordestitch.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidateRequest {

    @NotBlank(message = "user id can't be blank")
    private String userId;

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|in|co\\.uk|edu|gov|io)$",
            message = "Email should be valid and match allowed domains")
    private String emailAddress;

    @Pattern(regexp = "^$|^\\d{10}$", message = "Phone number must be exactly 10 digits or left empty")
    private String phoneNumber;

    @NotBlank(message = "Otp Password is mandatory")
    @Pattern(regexp = "\\d{6}", message = "OTP Password must be exactly 6 digits")
    private String otpCode;
}
