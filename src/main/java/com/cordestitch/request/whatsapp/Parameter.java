package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter {

    private String type;

    private String text;

    private String payload;

    @JsonProperty("date_time")
    private DateTimeParameter dateTimeParameter;

    @JsonProperty("image")
    private ImageParameter imageParameter;
}