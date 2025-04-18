package com.cordestitch.request.webhook;

import lombok.Data;

import java.util.List;

@Data
public class EntryRequest {

    private String id;

    private List<ChangeRequest> changes;
}