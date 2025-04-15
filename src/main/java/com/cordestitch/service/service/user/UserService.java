package com.cordestitch.service.service.user;

import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.request.user.LoginRequest;
import com.cordestitch.request.user.UserBankDetailsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.response.user.LoginResponse;
import com.cordestitch.response.user.UserBankDetailsResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface UserService {

    LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);

    SuccessResponse addAddress(AddressRequest addressRequest);

    List<AddressResponse> getAddresses(String userId);

    SuccessResponse updateAddress(AddressRequest addressRequest);

    SuccessResponse deleteAddress(String userId, String addressId);

    SuccessResponse logout(HttpServletResponse response);

    SuccessResponse addBankDetails(UserBankDetailsRequest request);

    List<UserBankDetailsResponse> getAllUserBankDetails(String userId);

    SuccessResponse deleteBankDetails(String userBankId, String userId);
}
