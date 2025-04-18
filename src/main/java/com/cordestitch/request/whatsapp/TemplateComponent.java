package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TemplateComponent {

    private String type;

    @JsonProperty("sub_type")
    private String subType;

    private Integer index;

    private List<Parameter> parameters;
}