package com.cordestitch.request.webhook;

import lombok.Data;

@Data
public class ChangeRequest {

    private ValueRequest value;

    private String field;
}