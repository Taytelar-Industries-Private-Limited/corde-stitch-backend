package com.cordestitch.response.product;

import com.cordestitch.entity.product.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilterResponse {

    private Category category;

    private SubCategory subCategory;

    private Product product;

    private StockQuantity stockQuantity;

    private ColorQuantity colorQuantity;
}