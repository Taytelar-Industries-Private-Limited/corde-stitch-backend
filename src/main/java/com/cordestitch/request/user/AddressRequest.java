package com.cordestitch.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    private String addressId;

    private String userId;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Pattern(regexp ="^[a-zA-Z ]+$", message = "First name must contain only alphabetic characters and spaces")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Last name must contain only alphabetic characters and spaces")
    private String lastName;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Building name is mandatory")
    private String buildingName;

    @NotBlank(message = "Street name is mandatory")
    private String streetName;

    @NotBlank(message = "City name is mandatory")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "City name must contain only alphanumeric characters")
    private String cityName;

    @NotBlank(message = "State name is mandatory")
    private String stateName;

    @NotBlank(message = "Country name is mandatory")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Country name must contain only alphanumeric characters")
    private String countryName;

    @NotBlank(message = "Pin code is mandatory")
    @Size(max = 6,message = "The provided pin code must be valid")
    @Pattern(regexp = "^\\d{6}$", message = "The pin code must be exactly 6 digits")
    private String pinCode;

    @Pattern(regexp = "^\\d{10}$", message = "Alternate phone number must be exactly 10 digits")
    private String altPhone;

    private String typeOfAddress;

    private String landMark;

}