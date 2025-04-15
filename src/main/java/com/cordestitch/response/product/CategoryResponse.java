package com.cordestitch.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String categoryName;
    private String categoryDescription;
    private List<SubCategoryResponse> subCategoryResponses;
}
