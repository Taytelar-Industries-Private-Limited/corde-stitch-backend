package com.cordestitch.request.customization.admincustomization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddFabricRequest {

    @NotBlank(message = "Customization ID cannot be blank")
    private String customizationId;

    @NotEmpty(message = "Fabric details cannot be empty")
    @NotNull(message = "Fabric details cannot be null")
    @Valid
    private List<FabricRequest> fabricRequestList;
}