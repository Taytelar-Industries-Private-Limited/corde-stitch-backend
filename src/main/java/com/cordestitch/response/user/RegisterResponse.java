package com.cordestitch.response.user;

import lombok.Data;

@Data
public class RegisterResponse {

    private String message;

    private String id;

    private String phoneNumber;

    private Integer statusCode;
}
