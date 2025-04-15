package com.cordestitch.request.whatsapp;

import lombok.Data;

@Data
public class InteractiveButtonRequest {

    private String type;

    private ReplyRequest reply;
}