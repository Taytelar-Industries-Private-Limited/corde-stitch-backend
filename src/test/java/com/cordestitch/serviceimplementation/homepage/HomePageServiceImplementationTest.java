package com.cordestitch.serviceimplementation.homepage;


import com.cordestitch.entity.homepage.HomePageEntity;
import com.cordestitch.entity.product.*;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.repository.homepage.HomePageRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.product.SubCategoryRepository;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.homepage.HomePageResponse;
import com.cordestitch.service.serviceimplementation.homepage.HomePageServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomePageServiceImplementationTest {
    @InjectMocks
    private HomePageServiceImplementation homePageServiceImplementation;

    @Mock
    private  HomePageRepository homePageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SubCategoryRepository subCategoryRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private Generator generator;

    private static final String TRENDING = "Trending";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadHomePageImages_Success() throws Exception {
        String folderName = "folder";
        String description = "Home page of cordestitch";
        MultipartFile file = mock(MultipartFile.class);
        String fileName = "fileAbc.doc";
        String bucketName = "test-bucket";
        Field bucketNameField = HomePageServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(homePageServiceImplementation, bucketName);
        String fileUrl = "http://example.com/folder/images/" + fileName;
        when(file.getOriginalFilename()).thenReturn(fileName);
        InputStream imageInputStream = new ByteArrayInputStream("dummy-image-content".getBytes());
        when(file.getInputStream()).thenReturn(imageInputStream);
        when(file.getSize()).thenReturn((long) "dummy-image-content".getBytes().length);
        S3Utilities s3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(new URL(fileUrl));
        when(homePageRepository.findByDescription(description)).thenReturn(Optional.empty());
        when(generator.generateId(Constants.HOME_PAGE_ID)).thenReturn("generatedId");
        SuccessResponse response = homePageServiceImplementation.uploadHomePageImages(folderName, file, description);
        assertEquals(Constants.HOME_PAGE_SUCCESS, response.getMessage());

    }

    @Test
    void uploadHomePageImages_Success_When_HomePageEntity_Is_Not_Empty() throws Exception {
        String folderName = "folder";
        String description = "Home page of cordestitch";
        MultipartFile file = mock(MultipartFile.class);
        String fileName = "fileAbc.doc";
        String bucketName = "test-bucket";
        Field bucketNameField = HomePageServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(homePageServiceImplementation, bucketName);
        String fileUrl = "http://example.com/folder/images/" + fileName;
        when(file.getOriginalFilename()).thenReturn(fileName);
        InputStream imageInputStream = new ByteArrayInputStream("dummy-image-content".getBytes());
        when(file.getInputStream()).thenReturn(imageInputStream);
        when(file.getSize()).thenReturn((long) "dummy-image-content".getBytes().length);
        S3Utilities s3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(new URL(fileUrl));
        when(homePageRepository.findByDescription(description)).thenReturn(Optional.of(getHomePageEntity()));
        when(generator.generateId(Constants.HOME_PAGE_ID)).thenReturn("generatedId");
        SuccessResponse response = homePageServiceImplementation.uploadHomePageImages(folderName, file, description);
        assertEquals(Constants.HOME_PAGE_SUCCESS, response.getMessage());

    }

    @Test
    void getAllHomePageImages_Success_When_OptionalHomePageEntityList_Is_Empty(){
        when(subCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(homePageRepository.findAll()).thenReturn(new ArrayList<>());
        when(productRepository.findByProductStatus(any())).thenReturn(Optional.empty());
        HomePageResponse response = homePageServiceImplementation.getAllHomePageImages();
        assertEquals(new ArrayList<>(), response.getHomePageLandscapeImages());
    }

    @Test
    void getAllHomePageImages_Success_When_OptionalProducts_Are_Empty(){
        when(subCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(homePageRepository.findAll()).thenReturn(List.of(getHomePageEntity()));
        when(productRepository.findByProductStatus(any())).thenReturn(Optional.empty());
        HomePageResponse response = homePageServiceImplementation.getAllHomePageImages();
        assertEquals(new ArrayList<>(), response.getHomePageTrendingProducts());
    }

    @Test
    void getAllHomePageImages_Success_When_OptionalProductList_Is_Empty(){
        when(subCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(homePageRepository.findAll()).thenReturn(List.of(getHomePageEntity()));
        when(productRepository.findByProductStatus(any())).thenReturn(Optional.of(List.of()));
        HomePageResponse response = homePageServiceImplementation.getAllHomePageImages();
        assertEquals(new ArrayList<>(), response.getHomePageTrendingProducts());
    }

    @Test
    void getAllHomePageImages_Success(){
        when(subCategoryRepository.findAll()).thenReturn(getsubCategoryList());
        when(homePageRepository.findAll()).thenReturn(getHomePageEntityList());
        when(productRepository.findByProductStatus(any())).thenReturn(Optional.of(List.of(getProduct())));
        HomePageResponse response = homePageServiceImplementation.getAllHomePageImages();
        assertNotNull(response);
    }

    private List<SubCategory> getsubCategoryList(){
        List<SubCategory> subCategoryList = new ArrayList<>();
        SubCategory subCategoryA = new SubCategory();
        subCategoryA.setCategory(new Category());
        subCategoryA.setSubCategoryDescription("Home Page Collection festival");
        subCategoryA.setSubCategoryName("Home Page Collection");
        SubCategory subCategoryB = new SubCategory();
        subCategoryB.setCategory(new Category());
        subCategoryB.setSubCategoryDescription("Collection festival");
        subCategoryB.setSubCategoryName("Collection");
        subCategoryList.add(subCategoryA);
        subCategoryList.add(subCategoryB);
        subCategoryList.add(getSubCategory());
        return subCategoryList;
    }
    private SubCategory getSubCategory() {
        SubCategory subCategory = new SubCategory();
        subCategory.setCategory(new Category());
        subCategory.setSubCategoryDescription("Trending clothes");
        subCategory.setSubCategoryName("Trending shirts");
        return subCategory;
    }

    @Test
    void getAllHomePageImages_Success_Where_Entry_GetValue_Is_Not_EqualTo_1_Or_2(){
        when(subCategoryRepository.findAll()).thenReturn(new ArrayList<>());
        when(homePageRepository.findAll()).thenReturn(List.of(getHomePageEntity()));
        Optional<List<Product>> optionalProducts = Optional.of(List.of(getProduct()));
        optionalProducts.get().getFirst().setProductImages(getImageMap());
        when(productRepository.findByProductStatus(any())).thenReturn(optionalProducts);
        HomePageResponse response = homePageServiceImplementation.getAllHomePageImages();
        assertNotNull(response);
    }

    private List<ProductImage> getImageMap() {
        List<ProductImage> productDataResponses = new ArrayList<>();
        ProductImage response = new ProductImage();
        response.setColorName("Blue");
        response.setImageUrl("image1");
        response.setImagePriority(1);
        productDataResponses.add(response);

        return productDataResponses;
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
        product.setSubCategory(new SubCategory());
        product.setStockQuantities(List.of(getStockQuantity()));
        product.setProductImages(getImageMap());
        return product;
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

    private List<HomePageEntity> getHomePageEntityList(){
        List<HomePageEntity> homePageEntities = new ArrayList<>();
        HomePageEntity homePageA = new HomePageEntity();
        homePageA.setHomePageId("home456");
        homePageA.setHomePageImageUrl("home456.jpg");
        homePageA.setDescription("Home Page Collection");
        HomePageEntity homePageB = new HomePageEntity();
        homePageB.setHomePageId("home789");
        homePageB.setHomePageImageUrl("home789.jpg");
        homePageB.setDescription("Collection");
        homePageEntities.add(homePageA);
        homePageEntities.add(homePageB);
        homePageEntities.add(getHomePageEntity());
        return homePageEntities;
    }
    private HomePageEntity getHomePageEntity() {
        HomePageEntity homePage = new  HomePageEntity();
        homePage.setHomePageId("home123");
        homePage.setDescription(TRENDING);
        homePage.setHomePageImageUrl("home.jpg");
        return homePage;
    }

    @Test
    void uploadHomePageImages_IOException() throws Exception {
        // Arrange
        String folderName = "folder";
        String description = "Home page of cordestitch";
        MultipartFile file = mock(MultipartFile.class);
        String fileName = "fileAbc.doc";
        String bucketName = "test-bucket";
        Field bucketNameField = HomePageServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(homePageServiceImplementation, bucketName);
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenThrow(new IOException());

        S3UploadException exception = assertThrows(S3UploadException.class, ()->homePageServiceImplementation.uploadHomePageImages(folderName, file, description));
        assertEquals(Constants.UPLOAD_ERROR,exception.getMessage());

    }

}