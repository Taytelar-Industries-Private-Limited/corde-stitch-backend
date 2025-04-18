package com.cordestitch.response.user;

import com.cordestitch.response.token.AuthTokenResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String firstName;

    private String lastName;

    private String emailAddress;

    private boolean emailAddressVerified;

    private String phoneNumber;

    private boolean phoneNumberVerified;

    private String referralCode;

    private String userType;

    private String gender;

    private AuthTokenResponse authTokenResponse;
}
