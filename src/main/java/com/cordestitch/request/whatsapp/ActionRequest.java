package com.cordestitch.request.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionRequest {

    private String button;

    private List<SectionRequest> sections;

    private List<InteractiveButtonRequest> buttons;
}