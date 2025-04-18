package com.cordestitch.response.user;

import lombok.Data;

@Data
public class AddressResponse {

    private String addressId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String buildingName;

    private String streetName;

    private String cityName;

    private String stateName;

    private String countryName;

    private String pinCode;

    private String typeOfAddress;

    private String landMark;

}