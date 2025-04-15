package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueRequest {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    private MetadataRequest metadata;

    private List<StatusRequest> statuses;

    private List<ContactRequest> contacts;

    private List<MessageRequest> messages;
}