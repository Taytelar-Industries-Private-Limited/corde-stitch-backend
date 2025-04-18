package com.cordestitch.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterConditionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private String key;
    private List<String> options;
    private List<FilterItem> colorOptions;

    public FilterConditionResponse(String title, String key, List<String> options) {
        this.title = title;
        this.key = key;
        this.options = options;
    }
}