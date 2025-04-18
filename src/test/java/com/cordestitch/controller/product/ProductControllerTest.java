package com.cordestitch.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.product.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.product.AddProductResponse;
import com.cordestitch.response.product.FilterConditionResponse;
import com.cordestitch.response.product.ProductDataResponse;
import com.cordestitch.response.product.ProductResponse;
import com.cordestitch.service.service.product.ProductService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @MockBean
    public ProductService productService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void testAddProduct() throws Exception {
        AddProductRequest addProductRequest = getProductRequest();
        when(productService.addProduct(any(AddProductRequest.class))).thenReturn(new AddProductResponse());
        mvc.perform(post("/api/product/addProduct")
                        .content(objectMapper.writeValueAsString(addProductRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUploadProductFiles() throws Exception {
        String productId = "product1";
        String colorName = "Red";
        MockMultipartFile images = new MockMultipartFile("images", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());
        MockMultipartFile imagePriorities = new MockMultipartFile("images", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "video content".getBytes());


        when(productService.uploadProductFiles(any(),any(),any(),any(),any())).thenReturn(new SuccessResponse());
        mvc.perform(multipart("/api/product/uploadFiles")
                        .file(images) // Adding an image file
                        .file(imagePriorities) // Adding another image file
                        .file(video)  // Adding a video file
                        .param("productId", productId)
                        .param("colorName", colorName)// Adding a productId parameter
                        .param("imagePriorities", "1", "2"))
                .andExpect(status().isOk());// Adding image priorities

    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(new ProductResponse());
        mvc.perform(get("/api/product/getAllProducts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductByProductId() throws Exception {
        String productId = "product1";
        when(productService.getProductByProductId(productId)).thenReturn(new ProductDataResponse());
        mvc.perform(get("/api/product/getProductByProductId")
                        .param("productId", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateProduct() throws Exception {
        UpdateProductRequest updateProductRequest = getUpdatedProductRequest();
        when(productService.updateProduct(any())).thenReturn(new SuccessResponse());
        mvc.perform(put("/api/product/updateProduct")
                        .content(objectMapper.writeValueAsString(updateProductRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFilterProductData() throws Exception{
        ProductFilterRequest request = getProductFilterRequest();
        when(productService.filterProductData(any())).thenReturn(new ProductResponse());
        mvc.perform(post("/api/product/filterProductData")
                       .content(objectMapper.writeValueAsString(request))
                       .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllFilterCondition() throws Exception{
        when(productService.getAllFilterCondition()).thenReturn(List.of(new FilterConditionResponse("size", "size", List.of("32", "34"))));
        mvc.perform(get("/api/product/getAllFilterCondition")
                       .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
        updateProductRequest.setAddProductRequest(getProductRequest());
        updateProductRequest.setCategoryId("cat1");
        updateProductRequest.setSubCategoryId("subcat1");
        return updateProductRequest;
    }

    private AddProductRequest getProductRequest() {
        AddProductRequest addProductRequest = new AddProductRequest();
        addProductRequest.setProductName("Test Product");
        addProductRequest.setProductStretchType("flexible");
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
}