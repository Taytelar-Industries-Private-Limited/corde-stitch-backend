package com.cordestitch.response.customization;

import com.cordestitch.entity.customization.ButtonTypes;
import com.cordestitch.entity.customization.CustomizationAttribute;
import com.cordestitch.entity.customization.PocketTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomizationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String customizationId;
    private String pantType;
    private List<Integer> trueWaistMeasurements;
    private List<Integer> pantInSeamLengths;
    private List<Integer> pantOutSeamLengths;
    private List<CustomizationAttribute> fitTypes;
    private List<CustomizationAttribute> riseTypes;
    private List<CustomizationAttribute> pantPleatsTypes;
    private List<CustomizationAttribute> pantCuffsTypes;
    private PocketTypes pocketTypes;
    private ButtonTypes buttonTypes;
    private List<CustomizationAttribute> flyTypes;
    private List<FabricResponse> fabrics;
    private List<CustomizationAttribute> customizationImages;
}
