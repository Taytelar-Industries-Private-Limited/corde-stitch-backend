package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Conversation {

    private String id;

    @JsonProperty("expiration_timestamp")
    private String expirationTimestamp;

    private OriginRequest origin;
}