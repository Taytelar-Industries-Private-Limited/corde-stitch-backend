package com.cordestitch.request.whatsapp;

import lombok.Data;

@Data
public class InteractiveRequest {

    private String type;

    private HeaderRequest header;

    private BodyRequest body;

    private ActionRequest action;
}