package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorQuantityResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String color;
    private String colorCode;
    private Integer quantity;
    private Map<String, Integer> imageUrls;
}
