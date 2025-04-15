package com.cordestitch.response.loyalty;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoyaltyPointsResponse {

    private String loyaltyId;

    private Double totalLoyaltyPoints;

    private Double totalExpiredPoints;

    private Double totalRedeemedPoints;

    private LocalDateTime lastUpdated;

    private List<LoyaltyPointsTransactionResponse> loyaltyPointsTransactionResponses;

}
