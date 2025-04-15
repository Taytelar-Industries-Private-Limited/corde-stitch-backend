package com.cordestitch.service.service.product;

import com.cordestitch.request.product.AddProductRequest;
import com.cordestitch.request.product.ProductFilterRequest;
import com.cordestitch.request.product.UpdateProductRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.product.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    AddProductResponse addProduct(AddProductRequest request);

    ProductResponse getAllProducts();

    SuccessResponse uploadProductFiles(String productId, String colorName, MultipartFile[] images, Integer[] imagePriorities, MultipartFile video);

    ProductDataResponse getProductByProductId(String productId);

    SuccessResponse updateProduct(UpdateProductRequest request);

    ProductResponse filterProductData(ProductFilterRequest request);

    List<FilterConditionResponse> getAllFilterCondition();

    List<FilterDataResponse> getProductFilterData(String productId);
}
