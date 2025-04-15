package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.cordestitch.request.webhook.TextRequest;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppMessageRequest {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    private String to;

    private String type;

    private TextRequest text;

    private MessageTemplate template;

    private InteractiveRequest interactive;
}