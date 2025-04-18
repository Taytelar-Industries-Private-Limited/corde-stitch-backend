package com.cordestitch.response.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionStatusResponse {

    private String message;

    private boolean aboutToExpire;
}
