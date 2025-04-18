package com.cordestitch.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String categoryDescription;

    @NotBlank(message = "Subcategory name is required")
    private String subCategoryName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String subCategoryDescription;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Product status is required")
    private String productStatus;

    @NotBlank(message = "Product description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String productDescription;

    @NotBlank(message = "Material type is required")
    private String productMaterialType;

    @NotBlank(message = "Product pattern is required")
    private String productPattern;

    @NotBlank(message = "Product stretch type is required")
    private String productStretchType;

    @NotNull(message = "Product offer percentage is required")
    @DecimalMin(value = "0.1", message = "Product offer percentage must be greater than 0")
    private Double productOfferPercentage;

    @NotNull(message = "Stock quantities are required")
    @Size(min = 1, message = "At least one stock quantity must be provided")
    @Valid
    private List<StockQuantityRequest> stockQuantities;
}