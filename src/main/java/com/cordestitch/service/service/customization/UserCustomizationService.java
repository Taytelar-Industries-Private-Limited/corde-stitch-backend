package com.cordestitch.service.service.customization;

import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.request.customization.usercustomization.CustomizedAddDataRequest;
import com.cordestitch.request.customization.usercustomization.CustomizedCartItemRequest;
import com.cordestitch.request.customization.usercustomization.UpdateCartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizedAddDataResponse;

public interface UserCustomizationService {
    SuccessResponse addCustomizedDataToCart(CustomizedAddDataRequest customizedDataRequest, CustomizedCartItemRequest customizedCartItemRequest, String userId);

    SuccessResponse addToCart(CustomizedCartItemRequest request, CustomizedAddDataResponse customizedAddDataResponse, UserEntity userEntity);

    SuccessResponse deleteCustomizedCartItem(String userId, String customizedCartItemId);

    SuccessResponse updateCustomizedCartItem(UpdateCartItemRequest cartRequest);
}