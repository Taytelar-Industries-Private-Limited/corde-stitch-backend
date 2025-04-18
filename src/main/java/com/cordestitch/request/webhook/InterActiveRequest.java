package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterActiveRequest {

    private String type;

    @JsonProperty("list_reply")
    private ListReplyRequest listReply;

    @JsonProperty("button_reply")
    private ListReplyRequest buttonReply;
}