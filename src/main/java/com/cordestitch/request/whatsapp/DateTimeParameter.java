package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DateTimeParameter {

    @JsonProperty("fallback_value")
    private String fallbackValue;
}