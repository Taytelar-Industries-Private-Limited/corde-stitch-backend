package com.cordestitch.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponse {

    private String firstName;

    private String lastName;

    private String gender;

    private String emailAddress;

    private boolean emailAddressVerified;

    private String phoneNumber;

    private boolean phoneNumberVerified;

    private String referralCode;

    private String userType;

    private List<AddressResponse> addressResponses;
}
