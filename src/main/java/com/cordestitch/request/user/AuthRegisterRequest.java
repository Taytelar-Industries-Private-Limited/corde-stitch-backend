package com.cordestitch.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRegisterRequest {

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|in|co\\.uk|edu|gov|io)$",
            message = "Email should be valid and match allowed domains")
    private String emailAddress;

    @NotBlank(message = "User Type cannot be null or empty")
    @Pattern(regexp = "customer|affiliate|admin", message = "User Type must be either 'customer' or 'affiliate' or 'admin'")
    private String userType;

    @NotBlank(message = "Auth Type cannot be null or empty")
    @Pattern(regexp = "google|facebook|appleiOS", message = "Auth Type must be either 'google' or 'facebook' or 'appleiOS'")
    private String authType;

}