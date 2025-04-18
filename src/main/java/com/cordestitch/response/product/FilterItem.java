package com.cordestitch.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String colorCode;

    private String colorName;
    
}