package com.cordestitch.request.customization.admincustomization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AttributeTypeRequestList {

    @Valid
    @NotEmpty(message = "Attribute Type Request cannot be empty")
    private List<AttributeTypeRequest> attributeRequests;
}