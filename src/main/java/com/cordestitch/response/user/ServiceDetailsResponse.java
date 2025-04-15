package com.cordestitch.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDetailsResponse {

    private String serviceType;

    private Boolean serviceAvailable;

    private String operationalHours;

    private String message;
}
