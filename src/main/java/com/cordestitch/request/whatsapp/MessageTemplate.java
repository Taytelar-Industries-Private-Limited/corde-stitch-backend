package com.cordestitch.request.whatsapp;


import lombok.Data;

import java.util.List;

@Data
public class MessageTemplate {

    private String name;

    private TemplateLanguage language;

    private List<TemplateComponent> components;
}