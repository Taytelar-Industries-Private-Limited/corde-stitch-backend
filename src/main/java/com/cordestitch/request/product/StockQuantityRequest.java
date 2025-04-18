package com.cordestitch.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockQuantityRequest {

    @NotNull(message = "Size cannot be blank")
    private Integer size;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.1", message = "Product price must be greater than 0")
    private Double productPrice;

    @NotNull(message = "Color-Quantity list cannot be null")
    @Valid
    private List<ColorQuantityRequest> colorQuantities;
}
