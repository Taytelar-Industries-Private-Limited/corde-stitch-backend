package com.cordestitch.serviceimplementation.product;

import com.cordestitch.entity.product.*;
import com.cordestitch.exception.order.CategoryNotFoundException;
import com.cordestitch.exception.order.SubCategoryNotFoundException;
import com.cordestitch.exception.product.ProductNotFoundException;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.exception.review.ResourceNotFoundException;
import com.cordestitch.repository.product.CategoryRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.product.SubCategoryRepository;
import com.cordestitch.request.product.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.product.*;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.response.review.ReviewResponse;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.service.serviceimplementation.product.ProductServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class ProductServiceImplementationTest {

    @InjectMocks
    private ProductServiceImplementation productServiceImplementation;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private Generator generator;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private EntityManager entityManager;

    private static final String PRODUCTS_CACHE_NAME = "productsCache";
    private static final String PRODUCT_CACHE_KEY = "listAllProduct";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(s3Client.utilities()).thenReturn(s3Utilities);
    }

    @Test
    void addProduct_Success() {
        AddProductRequest addProductRequest = getProductRequest();
        Optional<Category> optionalCategory = getOptionalCategory();
        Optional<SubCategory> optionalSubCategory = Optional.of(getSubCategory());
        when(categoryRepository.findByCategoryName(any())).thenReturn(optionalCategory);
        when(subCategoryRepository.findBySubCategoryName(any())).thenReturn(optionalSubCategory);
        when(entityManager.merge(any(Product.class))).thenReturn(getProduct());
        when(entityManager.merge(any(StockQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityManager.merge(any(ColorQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AddProductResponse addProductResponse = productServiceImplementation.addProduct(addProductRequest);
        assertEquals(Constants.PRODUCT_ADDED_SUCCESSFULLY, addProductResponse.getMessage());
    }

    @Test
    void addProduct_Success_When_OptionalCategory_And_OptionalSubCategory_Is_Null() {
        AddProductRequest addProductRequest = getProductRequest();
        Optional<Category> optionalCategory = Optional.empty();
        Optional<SubCategory> optionalSubCategory = Optional.empty();
        when(categoryRepository.findByCategoryName(any())).thenReturn(optionalCategory);
        when(subCategoryRepository.findBySubCategoryName(any())).thenReturn(optionalSubCategory);
        when(entityManager.merge(any(Product.class))).thenReturn(getProduct());
        when(entityManager.merge(any(StockQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityManager.merge(any(ColorQuantity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AddProductResponse addProductResponse = productServiceImplementation.addProduct(addProductRequest);
        assertEquals(Constants.PRODUCT_ADDED_SUCCESSFULLY, addProductResponse.getMessage());
    }

    @Test
    void uploadProductFiles_Success() throws Exception {
        String productId = "123";
        String bucketName = "test-bucket";
        String colorName = "blue";
        Field bucketNameField = ProductServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(productServiceImplementation, bucketName);
        Product product = new Product();
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        MultipartFile video = mock(MultipartFile.class);
        when(video.getOriginalFilename()).thenReturn("video.mp4");

        InputStream videoInputStream = new ByteArrayInputStream("dummy-video-content".getBytes());
        when(video.getInputStream()).thenReturn(videoInputStream);

        MultipartFile[] images = {image1, image2};
        Integer[] imagePriorities = {1, 2};

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/123/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/123/images/image2.jpg";
        String videoUrl = "https://s3.amazonaws.com/test-bucket/123/video/video.mp4";

        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URI(imageUrl1).toURL())
                .thenReturn(new URI(imageUrl2).toURL())
                .thenReturn(new URI(videoUrl).toURL());

        SuccessResponse response = productServiceImplementation.uploadProductFiles(productId, colorName, images, imagePriorities, video);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.IMAGES_AND_VIDEO_ADDED_SUCCESSFULLY, response.getMessage());

        verify(productRepository).save(product);
    }

    @Test
    void uploadProductFiles_Success_When_ProductImages_Are_Not_Null() throws Exception {
        String productId = "123";
        String bucketName = "test-bucket";
        String colorName = "blue";
        Field bucketNameField = ProductServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(productServiceImplementation, bucketName);
        Product product = new Product();
        product.setProductImages(new ArrayList<>());
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        MultipartFile video = mock(MultipartFile.class);
        when(video.getOriginalFilename()).thenReturn("video.mp4");

        InputStream videoInputStream = new ByteArrayInputStream("dummy-video-content".getBytes());
        when(video.getInputStream()).thenReturn(videoInputStream);

        MultipartFile[] images = {image1, image2};
        Integer[] imagePriorities = {1, 2};

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/123/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/123/images/image2.jpg";
        String videoUrl = "https://s3.amazonaws.com/test-bucket/123/video/video.mp4";

        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URI(imageUrl1).toURL())
                .thenReturn(new URI(imageUrl2).toURL())
                .thenReturn(new URI(videoUrl).toURL());

        SuccessResponse response = productServiceImplementation.uploadProductFiles(productId, colorName, images, imagePriorities, video);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals(Constants.IMAGES_AND_VIDEO_ADDED_SUCCESSFULLY, response.getMessage());

        verify(productRepository).save(product);
    }

    @Test
    void uploadProductFiles_ShouldThrowS3UploadException_WhenIOExceptionOccurs() throws Exception {
        String productId = "123";
        String colorName = "blue";
        Product product = new Product();
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        MultipartFile video = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");
        when(video.getOriginalFilename()).thenReturn("video.mp4");

        when(image1.getInputStream()).thenThrow(new IOException("Error uploading file to S3: {}"));

        MultipartFile[] images = {image1, image2};
        Integer[] imagePriorities = {1, 2};

        S3UploadException exception = assertThrows(S3UploadException.class,
                () -> productServiceImplementation.uploadProductFiles(productId, colorName, images, imagePriorities, video));

        assertEquals(Constants.UPLOAD_ERROR, exception.getMessage());

        verify(productRepository, never()).save(any());

    }

    @Test
    void uploadProductFiles_Product_Not_FoundException() {
        String productId = "Product123";
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productServiceImplementation.uploadProductFiles(productId, null, new MultipartFile[0], new Integer[0], null));
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getProductByProductId_Success() {
        String productId = "Product123";
        ProductDataResponse productDataResponse = getProductDataResponse();
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_CovertToProductResponse_With_ProductImages_Are_Null() {
        String productId = "Product123";
        ProductDataResponse productDataResponse = getProductDataResponse();
        Product product = getProduct();
        product.setProductImages(null);
        when(reviewService.getProductReviews(any())).thenReturn(getProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_CovertToProductResponse_With_ColorQuantities_Are_Null() {
        String productId = "Product123";
        ProductDataResponse productDataResponse = getProductDataResponse();
        Product product = getProduct();
        product.getStockQuantities().getFirst().setColorQuantities(null);
        when(reviewService.getProductReviews(any())).thenReturn(getProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_CovertToProductResponse_With_StockQuantities_Are_Null() {
        String productId = "Product123";
        ProductDataResponse productDataResponse = getProductDataResponse();
        Product product = getProduct();
        product.setStockQuantities(null);
        when(reviewService.getProductReviews(any())).thenReturn(getProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_When_CachedProductResponse_Is_Null() {
        String productId = "Product123";
        ProductDataResponse productDataResponse = getProductDataResponse();
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_When_Cache_Is_Not_Null() {
        String productId = "Product123";
        ProductDataResponse productDataResponse = getProductDataResponse();
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY, ProductResponse.class)).thenReturn(getProductResponse());
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_When_Cache_Is_Not_Null_And_OptionalProduct_IsPresent() {
        String productId = "1";
        ProductDataResponse productDataResponse = getProductDataResponse();
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY, ProductResponse.class)).thenReturn(getProductResponse());
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_When_Cache_Is_Not_Null_And_ProductImages_Are_Null() {
        String productId = "1";
        ProductDataResponse productDataResponse = getProductDataResponse();
        ProductResponse productResponse = getProductResponse();
        productResponse.getCategoryResponses().getFirst().getSubCategoryResponses().getFirst().getProductDataResponses().getFirst().setProductImageResponses(null);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY, ProductResponse.class)).thenReturn(productResponse);
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_When_Cache_Is_Not_Null_And_ColorQuantities_Are_Null() {
        String productId = "1";
        ProductDataResponse productDataResponse = getProductDataResponse();
        ProductResponse productResponse = getProductResponse();
        productResponse.getCategoryResponses().getFirst().getSubCategoryResponses().getFirst().getProductDataResponses().getFirst().getStockQuantityResponseList().getFirst().setColorQuantityResponses(null);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY, ProductResponse.class)).thenReturn(productResponse);
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }

    @Test
    void getProductByProductId_Success_When_Cache_Is_Not_Null_And_StockQuantities_Are_Null() {
        String productId = "1";
        ProductDataResponse productDataResponse = getProductDataResponse();
        ProductResponse productResponse = getProductResponse();
        productResponse.getCategoryResponses().getFirst().getSubCategoryResponses().getFirst().getProductDataResponses().getFirst().setStockQuantityResponseList(null);
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY, ProductResponse.class)).thenReturn(productResponse);
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(modelMapper.map(any(), any())).thenReturn(productDataResponse);
        ProductDataResponse response = productServiceImplementation.getProductByProductId(productId);
        assertEquals(productDataResponse.getProductId(), response.getProductId());
    }
    @Test
    void getProductByProductId_Exception_Product_Not_Found() {
        String productId = "Product123";
        when(reviewService.getProductReviews(any())).thenReturn(new ProductReviewResponse());
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productServiceImplementation.getProductByProductId(productId));
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
        when(productRepository.findByProductId(any())).thenThrow(ProductNotFoundException.class);
        assertThrows(ProductNotFoundException.class, () -> productServiceImplementation.getProductByProductId(productId));
    }

    @Test
    void getProductByProductId_Exception_Review_Not_Found() {
        String productId = "Product123";
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(getProduct()));
        when(reviewService.getProductReviews(any())).thenThrow(ResourceNotFoundException.class);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productServiceImplementation.getProductByProductId(productId));
        assertEquals(Constants.REVIEW_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateProduct_Success() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        Product product = getProduct();
        List<StockQuantity> stockQuantities = new ArrayList<>();
        stockQuantities.add(new StockQuantity());
        product.setStockQuantities(stockQuantities);
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        SuccessResponse expectedResponse = new SuccessResponse(Constants.PRODUCT_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
        SuccessResponse actualResponse = productServiceImplementation.updateProduct(updateProductRequest);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updateProduct_Success_When_ExistingStockQuantities_IsPresent() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        Product product = getProduct();
        List<StockQuantity> stockQuantities = new ArrayList<>();
        stockQuantities.add(getStockQuantity());
        product.setStockQuantities(stockQuantities);
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        SuccessResponse expectedResponse = new SuccessResponse(Constants.PRODUCT_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
        SuccessResponse actualResponse = productServiceImplementation.updateProduct(updateProductRequest);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updateProduct_Success_When_ExistingColorQuantity_IsPresent() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        Product product = getProduct();
        List<StockQuantity> stockQuantities = new ArrayList<>();
        stockQuantities.add(getStockQuantity());
        product.setStockQuantities(stockQuantities);
        List<ColorQuantity> colorQuantities = new ArrayList<>();
        ColorQuantity colorQuantity = getColorQuantityB();
        colorQuantities.add(colorQuantity);
        product.getStockQuantities().getFirst().setColorQuantities(colorQuantities);

        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        SuccessResponse expectedResponse = new SuccessResponse(Constants.PRODUCT_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
        SuccessResponse actualResponse = productServiceImplementation.updateProduct(updateProductRequest);
        assertEquals(expectedResponse, actualResponse);
    }

    private ProductReviewResponse getProductReviewResponse() {
        ProductReviewResponse productReviewResponse = new ProductReviewResponse();
        productReviewResponse.setProductId("Product123");
        productReviewResponse.setReviewAverage(4.0);
        productReviewResponse.setTotalNumberOfReviews(10);
        productReviewResponse.setReviews(List.of(getReviewResponse()));
        return productReviewResponse;
    }

    private ReviewResponse getReviewResponse() {
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.setReviewId("RID123");
        reviewResponse.setComment("good");
        reviewResponse.setRating(5);
        reviewResponse.setUserName("Jay");
        reviewResponse.setCreatedAt(LocalDateTime.now());
        reviewResponse.setImagePaths(List.of("image1"));
        reviewResponse.setVideoPaths(List.of("video1"));
        return reviewResponse;
    }

    private ColorQuantity getColorQuantityB() {
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("1");
        colorQuantity.setColor("black");
        colorQuantity.setColorCode("#000000");
        colorQuantity.setQuantity(5);
        return colorQuantity;
    }

    @Test
    void updateProduct_Success_When_ExistingColorQuantityOpt_Is_Not_Present() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        Product product = getProduct();
        List<StockQuantity> stockQuantities = new ArrayList<>();
        stockQuantities.add(getStockQuantity());
        product.setStockQuantities(stockQuantities);
        List<ColorQuantity> colorQuantities = new ArrayList<>();
        colorQuantities.add(getColorQuantityA());
        product.getStockQuantities().getFirst().setColorQuantities(colorQuantities);
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        SuccessResponse expectedResponse = new SuccessResponse(Constants.PRODUCT_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
        SuccessResponse actualResponse = productServiceImplementation.updateProduct(updateProductRequest);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void updateProduct_Exception_Product_Not_Found() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productServiceImplementation.updateProduct(updateProductRequest));
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateProduct_When_Category_And_SubCategoryDescription_Is_Null() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        updateProductRequest.getAddProductRequest().setCategoryDescription(null);
        updateProductRequest.getAddProductRequest().setSubCategoryDescription(null);
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productServiceImplementation.updateProduct(updateProductRequest));
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateProduct_When_Category_And_SubCategoryDescription_Is_Blank() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        updateProductRequest.getAddProductRequest().setCategoryDescription(" ");
        updateProductRequest.getAddProductRequest().setSubCategoryDescription(" ");
        Category category = new Category();
        category.setCategoryName("Test Category");
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryDescription("Test SubCategory");
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.of(subCategory));
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> productServiceImplementation.updateProduct(updateProductRequest));
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateProduct_Exception_SubCategory_Not_Found() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        Category category = new Category();
        category.setCategoryName("Test Category");
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.of(category));
        when(subCategoryRepository.findBySubCategoryId(any())).thenReturn(Optional.empty());
        SubCategoryNotFoundException exception = assertThrows(SubCategoryNotFoundException.class, () -> productServiceImplementation.updateProduct(updateProductRequest));
        assertEquals(Constants.SUBCATEGORY_NOT_FOUND + updateProductRequest.getAddProductRequest().getSubCategoryName(), exception.getMessage());
    }

    @Test
    void updateProduct_Exception_Category_Not_Found() {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        when(categoryRepository.findByCategoryId(any())).thenReturn(Optional.empty());
        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, () -> productServiceImplementation.updateProduct(updateProductRequest));
        assertEquals(Constants.CATEGORY_NOT_FOUND + updateProductRequest.getAddProductRequest().getCategoryName(), exception.getMessage());
    }

    @Test
    void getAllProducts_Success() {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category()));
        when(subCategoryRepository.findByCategory(any())).thenReturn(List.of(new SubCategory()));
        when(productRepository.findBySubCategory(any())).thenReturn(List.of(getProduct()));
        ProductResponse productResponse = productServiceImplementation.getAllProducts();
        assertNotNull(productResponse);
    }

    @Test
    void filterProductData_Success(){
        ProductFilterRequest request = getProductFilterRequest();
        when(productRepository.findProductsByFilters(any(),any(),any(),any(),any())).thenReturn(List.of(getProductFilterResponse()));
        ProductResponse productResponse = productServiceImplementation.filterProductData(request);
        assertNotNull(productResponse);
    }

    @Test
    void  getAllFilterCondition_Success(){
        String productStretchType = "Elastic";
        String productMaterialType = "polyester";
        String categoryName = "Formal pants";
        Integer size = 30;
        String color = "black";
        String colorCode = "#ffff";
        when(productRepository.fetchProductFilterTypes()).thenReturn(List.of(new FilterTypesResponse(productStretchType,productMaterialType,categoryName,size,color,colorCode)));
        List<FilterConditionResponse> response = productServiceImplementation.getAllFilterCondition();
        assertNotNull(response);
    }

    private ProductResponse getProductResponse() {
        ProductResponse response = new ProductResponse();
        response.setCategoryResponses(List.of(getCategoryResponse()));
        return response;
    }

    private CategoryResponse getCategoryResponse() {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryDescription("Trending pants");
        categoryResponse.setCategoryName("pants");
        categoryResponse.setCategoryId("CID123");
        categoryResponse.setSubCategoryResponses(List.of(getSubCategoryResponse()));
        return categoryResponse;
    }

    private SubCategoryResponse getSubCategoryResponse() {
        SubCategoryResponse subCategoryResponse = new SubCategoryResponse();
        subCategoryResponse.setSubCategoryDescription("Trending formals");
        subCategoryResponse.setSubCategoryId("SID123");
        subCategoryResponse.setSubCategoryName("formals");
        subCategoryResponse.setProductDataResponses(List.of(getProductDataResponse()));
        return subCategoryResponse;
    }

    private ProductFilterResponse getProductFilterResponse() {
        ProductFilterResponse productFilterResponse = new ProductFilterResponse();
        productFilterResponse.setProduct(getProduct());
        productFilterResponse.setCategory(getCategory());
        productFilterResponse.setColorQuantity(getColorQuantityA());
        productFilterResponse.setStockQuantity(getStockQuantity());
        productFilterResponse.setSubCategory(getSubCategory());
        return productFilterResponse;
    }

    private ProductFilterRequest getProductFilterRequest() {
        ProductFilterRequest productFilterRequest = new ProductFilterRequest();
        productFilterRequest.setProductStretchType(List.of("elastic"));
        productFilterRequest.setProductSubCategory(List.of("formal"));
        productFilterRequest.setColorCode(List.of("black"));
        productFilterRequest.setSize(List.of(30));
        productFilterRequest.setProductMaterialType(List.of("polyester"));
        return productFilterRequest;
    }

    private UpdateProductRequest getUpdatedProductRequest() {
        UpdateProductRequest updateProductRequest = new UpdateProductRequest();
        updateProductRequest.setProductId("product1");
        updateProductRequest.setAddProductRequest(getAddProductRequest());
        updateProductRequest.setCategoryId("cat1");
        updateProductRequest.setSubCategoryId("subcat1");
        return updateProductRequest;
    }

    private AddProductRequest getAddProductRequest() {
        AddProductRequest addProductRequest = new AddProductRequest();
        addProductRequest.setProductName("Test Product");
        addProductRequest.setProductDescription("This is a test product");
        addProductRequest.setProductPattern("plain");
        addProductRequest.setProductOfferPercentage(5.0);
        addProductRequest.setProductMaterialType("polyester");
        addProductRequest.setProductStatus("pending");
        addProductRequest.setStockQuantities(getListStockQuantities());
        addProductRequest.setCategoryName("test category");
        addProductRequest.setSubCategoryName("test subcategory");
        addProductRequest.setSubCategoryDescription("fit and fine");
        addProductRequest.setCategoryDescription("good cloth");
        return addProductRequest;
    }

    private List<StockQuantityRequest> getListStockQuantities() {
        List<StockQuantityRequest> stockQuantityRequests = new ArrayList<>();
        StockQuantityRequest stockQuantityRequest = new StockQuantityRequest();
        stockQuantityRequest.setSize(32);
        stockQuantityRequest.setProductPrice(100.00);
        stockQuantityRequest.setColorQuantities(getColorQuantityRequests());
        stockQuantityRequests.add(stockQuantityRequest);
        return stockQuantityRequests;

    }

    private List<ColorQuantityRequest> getColorQuantityRequests() {
        List<ColorQuantityRequest> colorQuantityRequests = new ArrayList<>();
        ColorQuantityRequest colorQuantityRequest = new ColorQuantityRequest();
        colorQuantityRequest.setColor("black");
        colorQuantityRequest.setColorCode("#000000");
        colorQuantityRequest.setQuantity(5);
        colorQuantityRequests.add(colorQuantityRequest);
        return colorQuantityRequests;
    }

    private ProductDataResponse getProductDataResponse() {
        ProductDataResponse productDataResponse = new ProductDataResponse();
        productDataResponse.setProductId("1");
        productDataResponse.setProductName("Pant");
        productDataResponse.setProductStatus("Pending");
        productDataResponse.setProductDescription("Formal pant");
        productDataResponse.setProductPattern("plain");
        productDataResponse.setProductMaterialType("polyester");
        productDataResponse.setProductOfferPercentage(5.0);
        productDataResponse.setStockQuantityResponseList(getListStockQuantityResponse());
        productDataResponse.setVideo("video");
        productDataResponse.setProductImageResponses(getImageMap());
        return productDataResponse;
    }

    private List<StockQuantityResponse> getListStockQuantityResponse() {
        List<StockQuantityResponse> list = new ArrayList<>();
        StockQuantityResponse stockQuantityResponse = new StockQuantityResponse();
        stockQuantityResponse.setSize(1);
        stockQuantityResponse.setProductPrice(100.0);
        stockQuantityResponse.setColorQuantityResponses(getColorQuantityResponse());
        list.add(stockQuantityResponse);
        return list;
    }

    private List<ColorQuantityResponse> getColorQuantityResponse() {
        List<ColorQuantityResponse> list = new ArrayList<>();
        ColorQuantityResponse colorQuantityResponse = new ColorQuantityResponse();
        colorQuantityResponse.setColor("red");
        colorQuantityResponse.setColorCode("FF0000");
        colorQuantityResponse.setQuantity(10);
        list.add(colorQuantityResponse);
        return list;
    }

    private List<ProductImageResponse> getImageMap() {
        List<ProductImageResponse> productDataResponses = new ArrayList<>();
        ProductImageResponse response = new ProductImageResponse();
        response.setColorName("Blue");
        response.setImageUrl("image1");
        response.setImagePriority(1);

        ProductImageResponse response1 = new ProductImageResponse();
        response1.setColorName("Black");
        response1.setImageUrl("image2");
        productDataResponses.add(response);
        productDataResponses.add(response1);
        return productDataResponses;
    }


    private Optional<Category> getOptionalCategory() {
        Category category = new Category();
        category.setCategoryId("1");
        category.setCategoryName("office wear");
        category.setCategoryDescription("office wear");
        category.setSubCategories(List.of(getSubCategory()));
        return Optional.of(category);
    }
    private Category getCategory(){
        Category category = new Category();
        category.setCategoryId("1");
        category.setCategoryName("office wear");
        category.setCategoryDescription("office wear");
        category.setSubCategories(List.of(getSubCategory()));
        return category;
    }

    private SubCategory getSubCategory() {
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryId("1");
        subCategory.setSubCategoryName("skin-fit");
        subCategory.setProducts(List.of(new Product()));
        subCategory.setSubCategoryDescription("comfortable skin-fit wear ");
        subCategory.setCategory(new Category());
        return subCategory;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setProductId("1");
        product.setProductName("Pant");
        product.setProductStatus("Pending");
        product.setProductDescription("Formal pant");
        product.setProductPattern("plain");
        product.setProductMaterialType("polyester");
        product.setProductOfferPercentage(5.0);
        product.setProductImages(getProductImageMap());
        product.setSubCategory(new SubCategory());
        product.setStockQuantities(List.of(getStockQuantity()));
        return product;
    }

    private List<ProductImage> getProductImageMap() {
        List<ProductImage> productDataResponses = new ArrayList<>();
        ProductImage response = new ProductImage();
        response.setColorName("Blue");
        response.setImageUrl("image1");
        response.setImagePriority(1);
        productDataResponses.add(response);

        return productDataResponses;
    }

    private StockQuantity getStockQuantity() {
        StockQuantity stockQuantity = new StockQuantity();
        stockQuantity.setSize(32);
        stockQuantity.setProductPrice(100.0);
        stockQuantity.setProduct(new Product());
        stockQuantity.setStockId("11");
        stockQuantity.setColorQuantities(getColorQuantity());
        return stockQuantity;
    }

    private ColorQuantity getColorQuantityA() {
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("1");
        colorQuantity.setStockQuantity(new StockQuantity());
        colorQuantity.setColor("white");
        colorQuantity.setColorCode("#ffffff");
        colorQuantity.setQuantity(5);
        return colorQuantity;
    }


    private List<ColorQuantity> getColorQuantity() {
        List<ColorQuantity> list = new ArrayList<>();
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("1");
        colorQuantity.setStockQuantity(new StockQuantity());
        colorQuantity.setColor("white");
        colorQuantity.setColorCode("#ffffff");
        colorQuantity.setQuantity(5);
        list.add(colorQuantity);
        return list;
    }

    private AddProductRequest getProductRequest() {
        AddProductRequest addProductRequest = new AddProductRequest();
        addProductRequest.setProductName("Pant");
        addProductRequest.setProductDescription("Formal pant");
        addProductRequest.setProductPattern("plain");
        addProductRequest.setProductStatus("Pending");
        addProductRequest.setProductMaterialType("polyester");
        addProductRequest.setProductOfferPercentage(5.0);
        addProductRequest.setCategoryName("office wear");
        addProductRequest.setCategoryDescription("designed for office use");
        addProductRequest.setSubCategoryName("skin-fit");
        addProductRequest.setSubCategoryDescription("comfortable skin-fit wear ");
        addProductRequest.setStockQuantities(getListStockQuantity());
        return addProductRequest;
    }

    private List<StockQuantityRequest> getListStockQuantity() {
        List<StockQuantityRequest> list = new ArrayList<>();
        StockQuantityRequest stockQuantityRequest = new StockQuantityRequest();
        stockQuantityRequest.setSize(32);
        stockQuantityRequest.setColorQuantities(getColorQuantityRequest());
        stockQuantityRequest.setProductPrice(100.0);
        list.add(stockQuantityRequest);
        return list;
    }

    private List<ColorQuantityRequest> getColorQuantityRequest() {
        List<ColorQuantityRequest> list = new ArrayList<>();
        ColorQuantityRequest colorQuantityRequest = new ColorQuantityRequest();
        colorQuantityRequest.setColor("white");
        colorQuantityRequest.setColorCode("#ffffff");
        colorQuantityRequest.setQuantity(5);
        list.add(colorQuantityRequest);
        return list;
    }

}