package com.cordestitch.response.loyalty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PointsRedemptionResponse {

    private Double totalRedeemablePoints;

    private String message;

    private int statusCode;
}
