package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageRequest {

    private ContextRequest context;

    private String from;

    private String id;

    private String timestamp;

    private String type;

    private TextRequest text;

    private ButtonRequest button;

    private InterActiveRequest interactive;
}