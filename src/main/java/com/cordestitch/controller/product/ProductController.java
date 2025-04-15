package com.cordestitch.controller.product;

import com.cordestitch.request.product.AddProductRequest;
import com.cordestitch.request.product.ProductFilterRequest;
import com.cordestitch.request.product.UpdateProductRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.product.*;
import com.cordestitch.service.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Endpoint to add a new product to the inventory.

     * This endpoint accepts a request to create and add a new product, including its details such as name, description, material type, pattern, price, stock quantities, and associated images and video. The product is also associated with a category and subcategory.

     * The request payload should be submitted as a multipart form data, including:
     * - **Product Details**: Product name, description, material type, pattern, price, stock quantities, etc.
     * - **Images**: A map of image files with associated priority values.
     * - **Video**: A video file related to the product.

     * The method processes the request using the `ProductService` to save the product information, upload images and videos to an S3 bucket, and associate the product with the provided category and subcategory.
     *
     * @param request The request object containing product details, images, and video.
     * @return ResponseEntity<SuccessResponse> - A ResponseEntity containing a `SuccessResponse` object with HTTP status 200 OK if the product is successfully added.
     *
     */
    @PostMapping("/addProduct")
    public ResponseEntity<AddProductResponse> addProduct(@Valid @RequestBody AddProductRequest request) {
        AddProductResponse response = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Endpoint to upload images and a video for a specific product.

     * This endpoint allows uploading multiple images with associated priority values and a video file related to a product identified by its `productId`. The images and video are uploaded to an external storage (e.g., AWS S3), and their metadata is associated with the specified product.

     * **Request Parameters**:
     * - **productId**: The unique identifier of the product to which the files are associated.
     * - **images**: Array of image files to be uploaded.
     * - **imagePriorities**: Array of priority values corresponding to each image.
     * - **video**: The video file to be uploaded.

     * **Response**: A `SuccessResponse` object with HTTP status 200 OK if the files are successfully uploaded and processed.
     *
     * @param productId The unique identifier of the product.
     * @param images Array of image files.
     * @param imagePriorities Array of priority values for images.
     * @param video The video file.
     * @return ResponseEntity<SuccessResponse> - A `ResponseEntity` containing a `SuccessResponse` object with HTTP status 200 OK.
     */
    @PostMapping("/uploadFiles")
    public ResponseEntity<SuccessResponse> uploadProductFiles(
            @RequestParam("productId") String productId,
            @RequestParam("colorName") String colorName,
            @RequestParam("images") MultipartFile[] images,
            @RequestParam("imagePriorities") Integer[] imagePriorities,
            @RequestParam("video") MultipartFile video) {

        SuccessResponse response = productService.uploadProductFiles(productId, colorName, images, imagePriorities, video);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint to retrieve all products along with their associated categories and subcategories.

     * This endpoint fetches a hierarchical structure of product data. It includes:
     * - **Categories**: Main product categories like "Pants," "Shirts," etc.
     * - **Subcategories**: Subdivisions within each category such as "Formal Pants," "Jeans," etc.
     * - **Products**: Detailed information about products listed under each subcategory, including name, description, material type, pattern, price, stock quantities, and URLs for images and videos.

     * **Response**: A `ProductResponse` object with HTTP status 200 OK containing the product and category data.
     *
     * @return ResponseEntity<ProductResponse> - A `ResponseEntity` containing the `ProductResponse` object with HTTP status 200 OK.
     */
    @GetMapping("/getAllProducts")
    public ResponseEntity<ProductResponse> getAllProducts() {
        long start = System.currentTimeMillis();
        ProductResponse response = productService.getAllProducts();
        long end = System.currentTimeMillis();
        log.info("Product Response(getAllProducts controller) retrieval time: {} ms", end - start);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * API to get the details of a product based on its product ID.
     * @param productId The unique identifier of the product.
     * @return ResponseEntity containing the product details wrapped in a ProductDataResponse object
     *         with an HTTP status of OK (200).
     */
    @GetMapping("/getProductByProductId")
    public ResponseEntity<ProductDataResponse> getProductByProductId(@RequestParam() String productId) {
        long startTime = System.currentTimeMillis();
        ProductDataResponse response = productService.getProductByProductId(productId);
        long endTime = System.currentTimeMillis();
        log.info("Product Response(getProductByProductId controller) retrieval time: {} ms", endTime - startTime);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * API to update the details of a product.
     * @param request The UpdateProductRequest object containing the new product data.
     * @return ResponseEntity containing a SuccessResponse indicating the result of the update operation
     *         with an HTTP status of OK (200).
     */
    @PutMapping("/updateProduct")
    public ResponseEntity<SuccessResponse> updateProduct(@Valid @RequestBody UpdateProductRequest request) {
        SuccessResponse response = productService.updateProduct(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * A request object for filtering products based on specific attributes.
     * This class defines the criteria available for filtering products. Each attribute can
     * be set individually, allowing for a dynamic filtering experience:
     * If any field is `null`, it is ignored in the filtering process, resulting in all
     * products being returned for that particular attribute.
     * - **productStretchType**: Specify the stretch type of the product, e.g., "No-stretch," "Two-way."
     * - **productSubCategory**: Select a specific subcategory of products.
     * - **productMaterialType**: Choose the material type, e.g., "Cotton," "Polyester."
     * - **size**: Set a specific size for filtering products, represented as an Integer.
     * - **colour**: Filter by color, e.g., "Red," "Blue," "Green."
     * Used as the request body for product filtering endpoints.
     */
    @PostMapping("/filterProductData")
    public ResponseEntity<ProductResponse> filterProductData(@RequestBody ProductFilterRequest request) {
        long start = System.currentTimeMillis();
        ProductResponse response = productService.filterProductData(request);
        long end = System.currentTimeMillis();
        log.info("Filtered Product Response(filterProductData controller) retrieval time: {} ms", end - start);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Fetches all available filtering conditions that users can apply to products.
     * This includes information on stretch types, subcategories, material types, available sizes,
     * and color options. It provides users with an overview of all filtering criteria, enabling them
     * to make informed decisions when selecting filters for product searches.
     * Returns a list of filter options organized by attribute type, supporting a flexible and
     * comprehensive product search experience.
     */
    @GetMapping("/getAllFilterCondition")
    public ResponseEntity<List<FilterConditionResponse>> getAllFilterCondition() {
        List<FilterConditionResponse>  response = productService.getAllFilterCondition();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getProductFilterData")
    public ResponseEntity<List<FilterDataResponse>> getProductFilterData(@NonNull @RequestParam String productId) {
        List<FilterDataResponse> response = productService.getProductFilterData(productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

