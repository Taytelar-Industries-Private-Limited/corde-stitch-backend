package com.cordestitch.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {

    @NotBlank(message = "Category Id is required")
    private String categoryId;

    @NotBlank(message = "Subcategory Id is required")
    private String subCategoryId;

    @NotBlank(message = "Product Id is required")
    private String productId;

    @Valid
    private AddProductRequest addProductRequest;
}
