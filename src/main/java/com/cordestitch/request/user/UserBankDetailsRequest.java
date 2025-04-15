package com.cordestitch.request.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBankDetailsRequest {

    @NotNull(message = "userId cannot be null")
    private String userId;

    @NotNull(message = "bankName cannot be null")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Bank name must contain only alphabetic characters and spaces")
    private String bankName;

    @NotNull(message = "accountNumber cannot be null")
    @Pattern(regexp = "\\d{9,18}", message = "Account number must be between 9 and 18 digits")
    private String accountNumber;

    @NotNull(message = "accountHolderName cannot be null")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Account holder name must contain only alphabetic characters and spaces")
    private String accountHolderName;

    @NotNull(message = "bankIfscCode cannot be null")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format. It must be 11 characters, start with 4 letters, followed by '0', and end with 6 alphanumeric characters.")
    private String bankIfscCode;
}
