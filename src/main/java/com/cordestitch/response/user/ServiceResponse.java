package com.cordestitch.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse {

    private String pinCode;

    private String cityName;

    private String stateName;

    private ServiceDetailsResponse serviceDetailsResponse;
}
