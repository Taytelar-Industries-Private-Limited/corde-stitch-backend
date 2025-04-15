package com.cordestitch.request.customization.usercustomization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @NotBlank(message = "user id can not be empty or null")
    private String userId;

    @NotBlank(message = "customized cart item id can not be empty or null")
    private String customizedCartItemId;

    @NotNull(message = "quantity can not be empty or null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}