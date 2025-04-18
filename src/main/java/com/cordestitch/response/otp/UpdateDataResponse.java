package com.cordestitch.response.otp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDataResponse {

    private String emailAddress;

    private boolean emailAddressVerified;

    private String phoneNumber;

    private boolean phoneNumberVerified;
}
