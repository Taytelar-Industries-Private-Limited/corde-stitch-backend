package com.cordestitch.response.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDetailsResponse {

    private String userId;
    private String cardId;
    private String lastFourDigits;
    private String cardHolderName;
    private String expirationDate;
    private String cardType;
    private String token;
}