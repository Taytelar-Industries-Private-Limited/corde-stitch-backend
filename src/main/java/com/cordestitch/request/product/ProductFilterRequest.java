package com.cordestitch.request.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductFilterRequest {

    private List<String> productStretchType;

    private List<String> productSubCategory;

    private List<String> productMaterialType;

    private List<Integer> size;

    private List<String> colorCode;

    public void convertEmptyToNull() {
        if (productStretchType != null && productStretchType.isEmpty()) productStretchType = null;
        if (productSubCategory != null && productSubCategory.isEmpty()) productSubCategory = null;
        if (productMaterialType != null && productMaterialType.isEmpty()) productMaterialType = null;
        if (size != null && size.isEmpty()) size = null;
        if (colorCode != null && colorCode.isEmpty()) colorCode = null;
    }
}