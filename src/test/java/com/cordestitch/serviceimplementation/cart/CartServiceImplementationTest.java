package com.cordestitch.serviceimplementation.cart;

import com.cordestitch.service.serviceimplementation.cart.CartServiceImplementation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.cart.CartEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.entity.cart.CustomizedCartItemEntity;
import com.cordestitch.entity.product.*;
import com.cordestitch.exception.cart.CartItemNotFoundException;
import com.cordestitch.repository.cart.CartRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.request.cart.CartRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartResponse;
import com.cordestitch.response.customization.CustomizationCartResponse;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class CartServiceImplementationTest {
    @InjectMocks
    private CartServiceImplementation cartServiceImplementation;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Generator generator;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToCart_Success() {
        CartRequest cartRequest = getCartRequest();
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
    }

    @Test
    void addToCart_Success_When_NewUser_Added() {
        CartRequest cartRequest = getCartRequest();
        when(cartRepository.findByUserId(any())).thenReturn(null);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
    }

    @Test
    void addToCart_AgainAddingSameProduct_Success() {
        CartRequest cartRequest = getCartRequest();
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
        Assertions.assertEquals(200, successResponse.getStatusCode());
    }

    @Test
    void addToCart_AddingDifferentProduct_Success() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setProductId("1234");
        cartRequest.getCartItemRequests().getFirst().setProductColor("Blue");
        cartRequest.getCartItemRequests().getFirst().setProductSize(36);
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
        Assertions.assertEquals(200, successResponse.getStatusCode());
    }

    @Test
    void addToCart_AddingDifferentProduct_Success_When_ProductIsEmpty() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setProductId("1234");
        cartRequest.getCartItemRequests().getFirst().setProductColor("Blue");
        cartRequest.getCartItemRequests().getFirst().setProductSize(36);
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
        Assertions.assertEquals(200, successResponse.getStatusCode());
    }

    @Test
    void addToCart_AddingDifferentProduct1_Success() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setProductSize(36);
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
        Assertions.assertEquals(200, successResponse.getStatusCode());
    }

    @Test
    void addToCart_AddingDifferentProduct2_Success() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setProductColor("Blue");
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("added to cart successfully", successResponse.getMessage(), "Added to Cart Successfully");
        Assertions.assertEquals(200, successResponse.getStatusCode());
    }

    @Test
    void addToCart_When_CartEntity_Is_Null() {
        CartRequest cartRequest = getCartRequest();
        when(cartRepository.findByUserId(any())).thenReturn(null);
        SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
        assertEquals("Cart not found for user", successResponse.getMessage(), "Added to Cart Successfully");
    }

    @Test
    void updateCartItem_Success() {
        CartRequest cartRequest = getCartRequest();
        when(cartRepository.findByUserId(any())).thenReturn(null);
        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> cartServiceImplementation.updateCartItem(cartRequest));
        assertEquals("Cart not found for user: ", exception.getMessage(), "Cart not found for user: user1");
    }

    @Test
    void updateCartItem_When_CartEntity_Not_Null() {
        CartRequest cartRequest = getCartRequest();
        CartEntity cartEntity = getCartEntity();
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCartItemId("cartItem1");
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        SuccessResponse successResponse = cartServiceImplementation.updateCartItem(cartRequest);
        assertEquals("Cart item updated successfully", successResponse.getMessage(), "Cart Item Updated Successfully");
    }

    @Test
    void updateCartItem_When_CartItemId_Is_Not_Equal() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setCartItemId("cartItem2");
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        SuccessResponse successResponse = cartServiceImplementation.updateCartItem(cartRequest);
        assertEquals("Cart Item Updated Successfully", successResponse.getMessage(), "Cart Item Updated Successfully");
    }

    @Test
    void updateCartItem_When_CartItemId_Is_Equal() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setCartItemId("cartItem1");
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        SuccessResponse successResponse = cartServiceImplementation.updateCartItem(cartRequest);
        assertEquals("Cart Item Updated Successfully", successResponse.getMessage(), "Cart Item Updated Successfully");
    }

    @Test
    void deleteCartItem_Success() {
        CartRequest cartRequest = getCartRequest();
        when(cartRepository.findByUserId(any())).thenReturn(null);
        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, () -> cartServiceImplementation.deleteCartItem(cartRequest));
        assertEquals(Constants.CART_NOT_FOUND + ": " + cartRequest.getUserId(), exception.getMessage(), Constants.CART_NOT_FOUND + ": " + cartRequest.getUserId());
    }

    @Test
    void deleteCartItem_When_CartEntity_Not_Null_And_CartItemId_Is_Not_Equal() {
        CartRequest cartRequest = getCartRequest();
        cartRequest.getCartItemRequests().getFirst().setCartItemId("cartItem2");
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        SuccessResponse successResponse = cartServiceImplementation.deleteCartItem(cartRequest);
        assertEquals("Cart Item Deleted Successfully", successResponse.getMessage(), "Cart Item Deleted Successfully");
    }

    @Test
    void deleteCartItem_When_CartEntity_Not_Null_And_CartItemId_Is_Equal() {
        CartRequest cartRequest = getCartRequest();
        CartEntity cartEntity = getCartEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        SuccessResponse successResponse = cartServiceImplementation.deleteCartItem(cartRequest);
        assertEquals("Cart item deleted successfully", successResponse.getMessage(), "Cart Item Deleted Successfully");
    }

    @Test
    void getCartItems_When_CartEntity_Is_Null() {
        String userId = "user1";
        when(cartRepository.findByUserId(any())).thenReturn(null);
        CartResponse response=cartServiceImplementation.getCartItems(userId);
        assertEquals("",new ArrayList<>(), response.getCartItemResponses());
    }

    @Test
    void getCartItems_When_CartEntity_Is_Not_Null() {
        String userId = "user1";
        CartEntity cartEntity = getCartEntity();
        CustomizedCartItemEntity customizedCartItemEntity=getCustomizedCartItemEntity();
        CustomizationCartResponse customizationCartResponse=new CustomizationCartResponse();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(objectMapper.convertValue(customizedCartItemEntity.getCustomizedProductDetails(),CustomizedAddDataResponse.class)).thenReturn(customizationCartResponse.getCustomizedAddDataResponse());
        CartResponse cartResponse = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", cartResponse.getCartId(), "cart1");
    }

    @Test
    void getCartItems_When_CartEntity_Is_Not_Null_With_CartItemEntityList_And_CustomizedCartItemList_Are_Null() {
        String userId = "user1";
        CartEntity cartEntity = getCartEntity();
        cartEntity.setCartItemEntityList(null);
        cartEntity.setCustomizedCartItemList(null);
        CustomizedCartItemEntity customizedCartItemEntity=getCustomizedCartItemEntity();
        CustomizationCartResponse customizationCartResponse=new CustomizationCartResponse();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(objectMapper.convertValue(customizedCartItemEntity.getCustomizedProductDetails(),CustomizedAddDataResponse.class)).thenReturn(customizationCartResponse.getCustomizedAddDataResponse());
        CartResponse cartResponse = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", cartResponse.getCartId(), "cart1");
    }

    @Test
    void getCartItems_When_CartEntity_Is_Not_Null_With_CartItemEntityList_And_CustomizedCartItemList_Are_Empty() {
        String userId = "user1";
        CartEntity cartEntity = getCartEntity();
        cartEntity.setCartItemEntityList(new ArrayList<>());
        cartEntity.setCustomizedCartItemList(new ArrayList<>());
        CustomizedCartItemEntity customizedCartItemEntity=getCustomizedCartItemEntity();
        CustomizationCartResponse customizationCartResponse=new CustomizationCartResponse();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(objectMapper.convertValue(customizedCartItemEntity.getCustomizedProductDetails(),CustomizedAddDataResponse.class)).thenReturn(customizationCartResponse.getCustomizedAddDataResponse());
        CartResponse cartResponse = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", cartResponse.getCartId(), "cart1");
    }

    @Test
    void testGetCartItems_Success_WhenProductIsEmpty() {
        String userId = "user1";
        when(cartRepository.findByUserId(any())).thenReturn(getCartEntity());
        when(productRepository.findByProductId(any())).thenReturn(Optional.empty());
        CartResponse response = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", response.getCartId(), "cart1");
    }

    @Test
    void testGetCartItems_Success() {
        String userId = "user1";
        CartEntity cartEntity = getCartEntity();
        cartEntity.getCartItemEntityList().getFirst().setProductSize(32);
        cartEntity.getCartItemEntityList().getFirst().setProductId("product1");
        cartEntity.getCartItemEntityList().getFirst().setProductColor("white");
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(productRepository.findByProductId(any())).thenReturn(getProductOptional());
        CartResponse response = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", response.getCartId(), "cart1");
    }

    @Test
    void testGetCartItems_Success_When_StockQuantityIsEmpty() {
        String userId = "user1";
        Product product = getProductOptional().get();
        product.setStockQuantities(new ArrayList<>());
        when(cartRepository.findByUserId(any())).thenReturn(getCartEntity());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        CartResponse response = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", response.getCartId(), "cart1");
    }

    @Test
    void testGetCartItems_Success_When_SizeIsDifferent() {
        String userId = "user1";
        Product product = getProductOptional().get();
        product.getStockQuantities().getFirst().setSize(40);
        when(cartRepository.findByUserId(any())).thenReturn(getCartEntity());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        CartResponse response = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", response.getCartId(), "cart1");
    }

    @Test
    void testGetCartItems_Success_When_ColorIsDifferent() {
        String userId = "user1";
        Product product = getProductOptional().get();
        product.getStockQuantities().getFirst().setSize(1);
        product.getStockQuantities().getFirst().getColorQuantities().getFirst().setColor("Dark Blue");
        when(cartRepository.findByUserId(any())).thenReturn(getCartEntity());
        when(productRepository.findByProductId(any())).thenReturn(Optional.of(product));
        CartResponse response = cartServiceImplementation.getCartItems(userId);
        assertEquals("cart1", response.getCartId(), "cart1");
    }

    private CustomizedCartItemEntity getCustomizedCartItemEntity() {
        CustomizedCartItemEntity cartItemEntity=new CustomizedCartItemEntity();
        cartItemEntity.setCustomizedCartItemId("customizedCartItem1");
        cartItemEntity.setPrice(10.00);
        cartItemEntity.setQuantity(2);
        cartItemEntity.setProductImageUrl("img.com");
        cartItemEntity.setProductOfferPercentage(5.0);
        cartItemEntity.setCustomizedProductDetails(getCustomizedProductDetails());
        return cartItemEntity;
    }

    private Optional<Product> getProductOptional() {
        Product product = new Product();
        product.setProductId("product1");
        product.setProductName("shirt");
        product.setProductDescription("formal white shirt");
        product.setProductPattern("plain");
        product.setProductStatus("Pending");
        product.setVideoUrl("video.com");
        product.setSubCategory(getSubCategory());
        product.setProductMaterialType("cotton");
        product.setProductOfferPercentage(5.0);
        product.setProductImages(getImageMap());
        product.setStockQuantities(getListStockQuantity(product));
        return Optional.of(product);
    }

    private List<StockQuantity> getListStockQuantity(Product product) {
        List<StockQuantity> stockQuantityList = new ArrayList<>();
        StockQuantity stockQuantity = new StockQuantity();

        stockQuantity.setProduct(product);

        stockQuantity.setSize(32);
        stockQuantity.setColorQuantities(getlistColorQuantity());

        stockQuantityList.add(stockQuantity);
        return stockQuantityList;
    }

    private SubCategory getSubCategory() {
        SubCategory subCategory = new SubCategory();
        subCategory.setSubCategoryId("subCategory1");
        subCategory.setSubCategoryName("formal");
        subCategory.setProducts(List.of(new Product()));
        subCategory.setSubCategoryDescription("formal cloth");
        subCategory.setCategory(new Category());
        return subCategory;
    }

    private List<ColorQuantity> getlistColorQuantity() {
        List<ColorQuantity> colorQuantityList = new ArrayList<>();
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("CQ1");
        colorQuantity.setColor("white");
        colorQuantity.setColorCode("#000000");
        colorQuantity.setQuantity(5);
        colorQuantityList.add(colorQuantity);
        return colorQuantityList;
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

    private CartEntity getCartEntity() {
        CartEntity cartEntity = new CartEntity();
        cartEntity.setCartId("cart1");
        cartEntity.setUserId("user1");
        cartEntity.setCartItemEntityList(getCartItemEntity());
        cartEntity.setCustomizedCartItemList(getCustomizedCartItemEntityList());
        return cartEntity;
    }

    private List<CustomizedCartItemEntity> getCustomizedCartItemEntityList() {
        List<CustomizedCartItemEntity> customizedCartItemEntities=new ArrayList<>();
        CustomizedCartItemEntity cartItemEntity=new CustomizedCartItemEntity();
        cartItemEntity.setCustomizedCartItemId("customizedCartItem1");
        cartItemEntity.setPrice(10.00);
        cartItemEntity.setQuantity(2);
        cartItemEntity.setProductImageUrl("img.com");
        cartItemEntity.setProductOfferPercentage(5.0);
        cartItemEntity.setCustomizedProductDetails(getCustomizedProductDetails());
        customizedCartItemEntities.add(cartItemEntity);
        return customizedCartItemEntities;
    }

    private Map<String, Object> getCustomizedProductDetails() {
        Map<String, Object> customizedProductDetails = new HashMap<>();
        customizedProductDetails.put("size", 1);
        customizedProductDetails.put("color", "white");
        customizedProductDetails.put("material", "cotton");
        return customizedProductDetails;
    }

    private List<CartItemEntity> getCartItemEntity() {
        List<CartItemEntity> cartItemEntities = new ArrayList<>();
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCartItemId("cartItem1");
        cartItemEntity.setPrice(10.00);
        cartItemEntity.setProductColor("white");
        cartItemEntity.setProductColorCode("#000000");
        cartItemEntity.setProductId("product1");
        cartItemEntity.setProductName("shirt");
        cartItemEntity.setProductSize(1);
        cartItemEntity.setQuantity(1);
        cartItemEntities.add(cartItemEntity);
        return cartItemEntities;
    }

    private CartRequest getCartRequest() {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setUserId("user1");
        cartRequest.setCartItemRequests(getListCartItemRequest());
        return cartRequest;
    }

    private List<CartItemRequest> getListCartItemRequest() {
        List<CartItemRequest> cartItemRequests = new ArrayList<>();
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setPrice(10.00);
        cartItemRequest.setProductColor("white");
        cartItemRequest.setProductColorCode("#000000");
        cartItemRequest.setProductId("product1");
        cartItemRequest.setProductName("shirt");
        cartItemRequest.setProductSize(1);
        cartItemRequest.setQuantity(1);
        cartItemRequests.add(cartItemRequest);
        return cartItemRequests;
    }
}