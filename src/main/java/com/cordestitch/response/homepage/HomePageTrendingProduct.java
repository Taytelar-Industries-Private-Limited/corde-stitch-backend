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
public class HomePageTrendingProduct implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private String productMaterialType;

    private List<String> imageUrls;

    private double price;
}
