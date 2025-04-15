package com.cordestitch.request.customization.admincustomization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class UpdateFabricRequest {

    @NotBlank(message = "Fabric ID cannot be blank")
    private String fabricId;

    @NotBlank(message = "Fabric colour type cannot be blank")
    private String fabricColor;

    @NotBlank(message = "Fabric color code cannot be blank")
    private String fabricColorCode;

    @NotBlank(message = "Fabric description type cannot be blank")
    private String fabricDescription;

    @NotNull(message = "Fabric price type cannot be null")
    @Positive(message = "Fabric price must be positive")
    private Double fabricPrice;

    @NotNull(message = "Fabric offer percentage type cannot be null")
    @Positive(message = "Fabric offer percentage must be positive")
    private Double productOfferPercentage;

    private List<String> imageUrl;
}