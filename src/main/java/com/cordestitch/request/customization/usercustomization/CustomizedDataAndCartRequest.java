package com.cordestitch.request.customization.usercustomization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizedDataAndCartRequest {

    @NotBlank(message = "user id can not be empty or null")
    private String userId;

    @Valid
    @NotNull(message = "Customized data cannot be null")
    private CustomizedAddDataRequest customizedData;

    @Valid
    @NotNull(message = "Cart item details cannot be null")
    private CustomizedCartItemRequest customizedCartItemRequest;
}