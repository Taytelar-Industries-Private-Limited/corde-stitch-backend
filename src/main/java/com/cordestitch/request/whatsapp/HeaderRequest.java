package com.cordestitch.request.whatsapp;

import lombok.Data;

@Data
public class HeaderRequest {

    private String type;

    private String text;
}