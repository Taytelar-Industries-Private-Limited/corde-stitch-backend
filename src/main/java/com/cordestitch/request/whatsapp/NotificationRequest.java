package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRequest {

    private String recipientNumber;

    private String userName;

    private String orderItemId;

    private String orderDate;
}