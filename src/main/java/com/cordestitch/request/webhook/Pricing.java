package com.cordestitch.request.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Pricing {

    private boolean billable;

    @JsonProperty("pricing_model")
    private String pricingModel;

    private String category;
}