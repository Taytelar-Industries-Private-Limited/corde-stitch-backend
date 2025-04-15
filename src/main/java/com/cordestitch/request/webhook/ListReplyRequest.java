package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListReplyRequest {

    private String id;

    private String title;

    private String description;
}