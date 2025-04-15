package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarkAsReadRequest {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    private String status;

    @JsonProperty("message_id")
    private String messageId;
}