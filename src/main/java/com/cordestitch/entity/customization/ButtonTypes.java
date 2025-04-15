package com.cordestitch.entity.customization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ButtonTypes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<CustomizationAttribute> frontButtonTypes;

    private List<CustomizationAttribute> backButtonTypes;

    private List<CustomizationAttribute> buttonTypeImagesUrl;
}