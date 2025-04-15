package com.cordestitch.response.homepage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomePageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<HomePageLandscapeImage> homePageLandscapeImages;

    private List<HomePageTrendingProduct> homePageTrendingProducts;

    private List<HomePageLandscapeImage> homePageSubCategoryImages;

    private List<HomePageLandscapeImage> homePageCustomizationImages;
}
