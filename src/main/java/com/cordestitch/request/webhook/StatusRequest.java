package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusRequest {

    private String id;

    private String status;

    private String timestamp;

    @JsonProperty("recipient_id")
    private String recipientId;

    private Conversation conversation;

    private Pricing pricing;
}