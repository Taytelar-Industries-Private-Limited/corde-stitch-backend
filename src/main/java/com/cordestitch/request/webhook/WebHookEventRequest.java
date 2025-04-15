package com.cordestitch.request.webhook;

import lombok.Data;

import java.util.List;

@Data
public class WebHookEventRequest {
    
    private String object;
    
    private List<EntryRequest> entry;
}