package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContactRequest {

    private ProfileRequest profile;

    @JsonProperty("wa_id")
    private String waId;
}