package com.cordestitch.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email address is mandatory")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|in|co\\.uk|edu|gov|io)$",
            message = "Email should be valid and match allowed domains")
    private String emailAddress;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "User Type cannot be null or empty")
    @Pattern(regexp = "customer|affiliate|admin", message = "User Type must be either 'customer' or 'affiliate' or 'admin'")
    private String userType;

    @Pattern(regexp = "^$|^[A-Z0-9]{6}$", message = "Referral code must be 6 uppercase alphanumeric characters or empty")
    @Size(max = 6, message = "Referral code cannot exceed 6 characters")
    private String referralCode;

}
