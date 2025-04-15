package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FilterTypesResponse {

    private String productStretchType;

    private String productMaterialType;

    private String subCategoryName;

    private Integer size;

    private String color;

    private String colorCode;

}
