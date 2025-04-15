package com.cordestitch.controller.customization;

import com.cordestitch.request.customization.usercustomization.CustomizedDataAndCartRequest;
import com.cordestitch.request.customization.usercustomization.UpdateCartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.customization.UserCustomizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userCustomization")
@RequiredArgsConstructor
public class UserCustomizationController {

    private final UserCustomizationService userCustomizationService;

    /**
     * This API endpoint is used to add customized data to the cart for a specified user.
     * It processes the incoming request containing the customized data and cart details,
     * invokes the appropriate service method to handle the addition, and returns a
     * success response if the operation is completed successfully.
     *
     * @param request A CustomizedDataAndCartRequest object containing the customized data
     *                and cart information for the user.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of the
     * add operation, including status and message.
     * HTTP Status Codes:
     * 200 OK - When the customized data is successfully added to the cart.
     * 400 BAD REQUEST - If the input request is invalid or fails validation.
     */
    @PostMapping("/addCustomizedDataToCart")
    public ResponseEntity<SuccessResponse> addCustomizedDataToCart(@Valid @RequestBody CustomizedDataAndCartRequest request) {
        SuccessResponse response = userCustomizationService.addCustomizedDataToCart(request.getCustomizedData(), request.getCustomizedCartItemRequest(), request.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * This API endpoint is used to update a customized cart item for a specified user.
     * It processes the incoming request containing the details of the cart item to be updated,
     * invokes the appropriate service method to handle the update, and returns a
     * success response if the operation is completed successfully.
     *
     * @param cartRequest An UpdateCartItemRequest object containing the details required
     *                    to update the customized cart item, including the user ID,
     *                    customized cart item ID, and updated properties (quantity).
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of the
     * update operation, including status and message.
     * HTTP Status Codes:
     * 200 OK - When the customized cart item is successfully updated in the cart.
     */
    @PatchMapping("/updateCustomizedCartItem")
    public ResponseEntity<SuccessResponse> updateCustomizedCartItem(@Valid @RequestBody UpdateCartItemRequest cartRequest) {
        SuccessResponse successResponse = userCustomizationService.updateCustomizedCartItem(cartRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }


    /**
     * This API endpoint is used to delete a customized cart item for a specified user.
     * It processes the incoming request containing the user ID and the customized cart item ID,
     * invokes the appropriate service method to handle the deletion, and returns a
     * success response if the operation is completed successfully.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @param customizedCartItemId A String representing the ID of the customized cart item
     *                              to be deleted from the user's cart.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of the
     * delete operation, including status and message.
     * HTTP Status Codes:
     * 200 OK - When the customized cart item is successfully deleted from the cart.
     * 404 NOT FOUND - If the cart or the specified customized cart item is not found.
     */
    @DeleteMapping("/deleteCustomizedCartItem")
    public ResponseEntity<SuccessResponse> deleteCustomizedCartItem(HttpServletRequest request, @RequestParam String customizedCartItemId) {
        String userId = (String) request.getAttribute("userId");
        SuccessResponse successResponse = userCustomizationService.deleteCustomizedCartItem(userId,customizedCartItemId);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
