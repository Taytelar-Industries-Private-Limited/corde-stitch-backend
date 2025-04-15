package com.cordestitch.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Pin code is mandatory")
    @Size(max = 6,message = "The provided pin code must be valid")
    @Pattern(regexp = "^\\d{6}$", message = "The pin code must be exactly 6 digits")
    private String pinCode;

    @NotBlank(message = "The Service Request Type cannot be null or empty")
    @Pattern(regexp = "alt|fit|delivery", message = "The Service Request Type must be either 'alt' or 'fit' or 'delivery'")
    private String serviceRequestType;

    private String productId;
}
