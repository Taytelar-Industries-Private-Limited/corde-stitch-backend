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
public class UpdateProfileRequest {

    @NotBlank(message = "user id can't be blank")
    private String userId;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Pattern(regexp = "^$|^male|female|others", message = "Gender must be male or female or others")
    private String gender;

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|in|co\\.uk|edu|gov|io)$",
            message = "Email should be valid and match allowed domains")
    private String emailAddress;

    @Pattern(regexp = "^$|^\\d{10}$", message = "Phone number must be exactly 10 digits or left empty")
    private String phoneNumber;
}
