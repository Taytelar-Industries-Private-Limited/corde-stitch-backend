package com.cordestitch.service.service.user;

import com.cordestitch.request.user.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.otp.UpdateDataResponse;
import com.cordestitch.response.user.UserDetailsResponse;

public interface AuthRegisterService {

    SuccessResponse updateEmailAddressOrPhoneNumber(UpdateRequest request);

    SuccessResponse validateOTPForEmailOrPhoneNumber(ValidateRequest request);

    UpdateDataResponse checkEmailOrPhoneNumberVerified(String userId);

    SuccessResponse updateProfile(UpdateProfileRequest request);

    UserDetailsResponse getUserDetails(String userId);

    SuccessResponse updateUserDetails(UpdateUserDetailsRequest request);
}