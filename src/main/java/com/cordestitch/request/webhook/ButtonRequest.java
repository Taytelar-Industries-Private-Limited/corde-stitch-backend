package com.cordestitch.request.webhook;

import lombok.Data;

@Data
public class ButtonRequest {

    private String payload;

    private String text;
}