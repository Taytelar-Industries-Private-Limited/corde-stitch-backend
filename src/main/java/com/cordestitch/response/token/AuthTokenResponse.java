package com.cordestitch.response.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenResponse {

    private String accessToken;

    private String tokenExpiryTime;

    private String refreshTokenExpiryTime;

    private int statusCode;
}