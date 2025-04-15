package com.cordestitch.controller.loyalty;

import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.loyalty.LoyaltyPointsResponse;
import com.cordestitch.response.loyalty.PointsRedemptionResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyPointController {

    private final LoyaltyPointsService loyaltyPointsService;

    /**
     * Endpoint to process a referral.
     * This endpoint accepts a referral code from the referrer and associates it with the current user.
     * The referral code is validated, and appropriate loyalty points are awarded or processed based on the business logic.
     * The method retrieves the user ID from the HTTP request attributes, processes the referral using the
     * `LoyaltyPointsService`, and returns a success response.
     */
    @PostMapping("/processReferral")
    public ResponseEntity<SuccessResponse> processReferral(@RequestParam String referrerCode, HttpServletRequest request){
        String userId = (String) request.getAttribute(Constants.USERID);
        SuccessResponse response = loyaltyPointsService.processReferral(referrerCode,userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Endpoint to process loyalty points for a specific order item.
     * This endpoint accepts an order item ID, processes the associated loyalty points, and ties the points to the current user.
     * The user ID is retrieved from the HTTP request attributes to ensure the points are credited to the correct user account.
     * The method leverages the `LoyaltyPointsService` to perform the business logic of processing loyalty points for the given order item.
     */
    @PostMapping("/processOrderItemLoyaltyPoints")
    public ResponseEntity<SuccessResponse> processOrderItemLoyaltyPoints(@RequestParam String orderItemId, HttpServletRequest request) {
        String userId = (String) request.getAttribute(Constants.USERID);
        SuccessResponse response = loyaltyPointsService.processOrderLoyaltyPoints(orderItemId, userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Endpoint to retrieve the loyalty points summary for the current user.
     * This endpoint fetches a summary of loyalty points associated with the currently authenticated user.
     * The user ID is extracted from the HTTP request attributes to ensure the data corresponds to the logged-in user.
     * The method calls the `LoyaltyPointsService` to obtain the loyalty points summary and returns it in the response.
     */
    @GetMapping("/getLoyaltyPointsSummary")
    public ResponseEntity<LoyaltyPointsResponse> getLoyaltyPointsSummary(HttpServletRequest request){
        String userId = (String) request.getAttribute(Constants.USERID);
        LoyaltyPointsResponse loyaltyPointsResponse = loyaltyPointsService.getLoyaltyPointsSummary(userId);
        return ResponseEntity.status(HttpStatus.OK).body(loyaltyPointsResponse);
    }

    /**
     * Endpoint to redeem money using loyalty points for the user.
     * This endpoint allows the currently authenticated user to convert their loyalty points into monetary value.
     * The user ID is extracted from the HTTP request attributes to ensure the operation is performed for the logged-in user.
     * The method calls the `LoyaltyPointsService` to process the redemption request based on the provided total redeemable points.
     * @return A ResponseEntity containing the status code and a success response object with the redemption details.
     */
    @PostMapping("/redeemMoney")
    public ResponseEntity<PointsRedemptionResponse> redeemMoney(HttpServletRequest request, @RequestParam Double totalRedeemablePoints){
        String userId = (String) request.getAttribute(Constants.USERID);
        PointsRedemptionResponse response = loyaltyPointsService.redeemMoneyFromPoints(userId,totalRedeemablePoints);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    /**
     * API Endpoint to Manually Trigger the Loyalty Points Processing Scheduler.

     * This endpoint is used to manually process loyalty points transactions for orders.
     * It checks for pending loyalty points transactions and updates their status based
     * on the order's delivery and return status. If an order is eligible, loyalty points
     * are credited to the user's account; otherwise, they are canceled.

     * **Note:** This is a manual invocation of the scheduled job that processes loyalty
     * points periodically in the background. It is intended for administrative or
     * debugging purposes.
     *
     * @return ResponseEntity containing a list of {@link SuccessResponse} objects with
     * details of processed transactions.

     * - HTTP 200 (OK): Indicates the operation was successful.
     * - The response body contains the list of loyalty points transactions processed
     *   during this invocation.
     */
    @GetMapping("/processLoyaltyPointsToAccount")
    public ResponseEntity<List<SuccessResponse>> entityProcessLoyaltyPointsToAccount(){
        List<SuccessResponse> responses = loyaltyPointsService.entityProcessLoyaltyPointsToAccount();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

}
