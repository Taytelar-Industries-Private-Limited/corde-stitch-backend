package com.cordestitch.request.customization.admincustomization;

import com.cordestitch.entity.customization.ButtonTypes;
import com.cordestitch.entity.customization.CustomizationAttribute;
import com.cordestitch.entity.customization.PocketTypes;
import com.cordestitch.validation.customization.PositiveIntegerList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCustomizationRequest {

    @NotBlank(message = "Customization ID cannot be blank")
    private String customizationId;

    @NotBlank(message = "Pant type cannot be blank")
    private String pantType;

    @NotEmpty(message = "True waist measurements cannot be empty")
    @PositiveIntegerList
    private List<Integer> trueWaistMeasurement;

    @NotEmpty(message = "Pant inSeam lengths cannot be empty")
    @PositiveIntegerList
    private List<Integer> pantInSeamLength;

    @NotEmpty(message = "Pant outSeam lengths cannot be empty")
    @PositiveIntegerList
    private List<Integer> pantOutSeamLength;

    @NotEmpty(message = "Fit types cannot be empty")
    @Valid
    private List<CustomizationAttribute> fitType;

    @NotEmpty(message = "Rise types cannot be empty")
    @Valid
    private List<CustomizationAttribute> riseType;

    @Valid
    @NotNull(message = "Pockets cannot be null")
    private PocketTypes pocketTypes;

    @Valid
    @NotNull(message = "Buttons cannot be null")
    private ButtonTypes buttonTypes;

    @NotEmpty(message = "Fly types cannot be empty")
    @Valid
    private List<CustomizationAttribute> flyType;

    @NotEmpty(message = "Pant pleats types cannot be empty")
    @Valid
    private List<CustomizationAttribute> pantPleatsType;

    @NotEmpty(message = "Pant cuff types cannot be empty")
    @Valid
    private List<CustomizationAttribute> pantCuffType;

    @NotEmpty(message = "Customization images types cannot be empty")
    @Valid
    private List<CustomizationAttribute> customizationImages;

    @NotEmpty(message = "Fabric details cannot be empty")
    @NotNull(message = "Fabric details cannot be null")
    @Valid
    private List<UpdateFabricRequest> fabric;
}