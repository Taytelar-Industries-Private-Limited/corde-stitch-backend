package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterDataResponse {

    private Integer size;

    private Double amount;

    private List<ColorDataResponse> colorDataResponses;
}
