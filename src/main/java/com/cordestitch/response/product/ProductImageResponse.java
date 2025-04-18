package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String colorName;
    private String imageUrl;
    private Integer imagePriority;

}
