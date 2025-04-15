package com.cordestitch.request.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDetailsRequest {

    @NotBlank(message = "user id can't be blank")
    private String userId;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "First name must contain only alphabetic characters and spaces")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Last name must contain only alphabetic characters and spaces")
    private String lastName;

    @Pattern(regexp = "^$|^male|female|others", message = "Gender must be male or female or others")
    private String gender;

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|in|co\\.uk|edu|gov|io)$",
            message = "Email should be valid and match allowed domains")
    private String emailAddress;

    @Valid
    private List<AddressRequest> addressRequests;
}
