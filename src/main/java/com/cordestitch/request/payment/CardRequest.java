package com.cordestitch.request.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardRequest {

    @NotBlank(message = "User ID is required.")
    private String userId;

    @NotBlank(message = "Card type is required.")
    @Pattern(regexp = "^(Visa|Mastercard|American Express|BAJAJ|Maestro|Rupay|Diners)$",
            message = "Card type must be one of the following: Visa, Mastercard, American Express, BAJAJ, Maestro, Rupay, Diners.")
    private String cardType;

    @NotBlank(message = "Last four digits are required.")
    @Size(min = 4, max = 4, message = "Last four digits must be 4 characters long.")
    @Pattern(regexp = "\\d{4}", message = "Last four digits must be numeric.")
    private String lastFourDigits;

    @NotBlank(message = "Expiration date is required.")
    @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Expiration date must be in MM/yy format.")
    private String expirationDate;

    @NotBlank(message = "Card holder name is required.")
    private String cardHolderName;

    @NotBlank(message = "Token is required.")
    private String token;
}