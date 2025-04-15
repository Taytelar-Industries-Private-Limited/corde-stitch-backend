package com.cordestitch.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBankDetailsResponse {

    private String userBankId;

    private String accountNumber;

    private String accountHolderName;

    private String bankIfscCode;

    private String bankName;
}
