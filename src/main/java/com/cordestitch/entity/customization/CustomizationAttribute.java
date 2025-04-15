package com.cordestitch.entity.customization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomizationAttribute implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String attributeId;

    private String attributeType;

    private List<String> attributeImagesUrl = new ArrayList<>();
}