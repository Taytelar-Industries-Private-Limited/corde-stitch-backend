package com.cordestitch.serviceimplementation.order;

import com.cordestitch.service.serviceimplementation.order.OrderServiceImplementation;
import com.cordestitch.service.serviceimplementation.order.OrderServiceMappingHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.cart.CartEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.entity.cart.CustomizedCartItemEntity;
import com.cordestitch.entity.customization.Fabric;
import com.cordestitch.entity.customization.UserCustomizationEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.order.OrderItemEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.payment.PaymentEntity;
import com.cordestitch.entity.payment.RefundEntity;
import com.cordestitch.entity.product.*;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.enums.*;
import com.cordestitch.exception.order.*;
import com.cordestitch.exception.payment.PaymentNotFoundException;
import com.cordestitch.exception.payment.RefundProcessException;
import com.cordestitch.exception.user.AddressNotFoundException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.repository.cart.CartRepository;
import com.cordestitch.repository.customization.UserCustomizationRepository;
import com.cordestitch.repository.order.OrderItemRepository;
import com.cordestitch.repository.order.OrderRepository;
import com.cordestitch.repository.order.ReturnRepository;
import com.cordestitch.repository.payment.PaymentRepository;
import com.cordestitch.repository.payment.RefundRepository;
import com.cordestitch.repository.product.ColorQuantityRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.repository.user.AddressRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.order.CancelOrderRequest;
import com.cordestitch.request.order.OrderItemRequest;
import com.cordestitch.request.order.OrderRequest;
import com.cordestitch.request.order.ReturnRequest;
import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizationCartResponse;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import com.cordestitch.response.order.*;
import com.cordestitch.response.product.*;
import com.cordestitch.response.review.ProductReviewResponse;
import com.cordestitch.service.service.loyalty.LoyaltyPointsService;
import com.cordestitch.service.service.review.ReviewService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.service.serviceimplementation.customization.UserCustomizationServiceImplementation;
import com.cordestitch.service.serviceimplementation.payment.PaymentServiceImpl;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class OrderServiceImplementationTest {
    @InjectMocks
    private OrderServiceImplementation orderServiceImplementation;

    @Mock
    private PaymentServiceImpl paymentServiceImpl;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private LoyaltyPointsService loyaltyPointsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ColorQuantityRepository colorQuantityRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private Generator generator;

    @Mock
    private IdEncryptor idEncryptor;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private Cache.ValueWrapper valueWrapper;

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private OrderServiceMappingHelper orderServiceMappingHelper;

    @Mock
    private UserCustomizationRepository userCustomizationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserCustomizationServiceImplementation serviceImplementation;

    @Mock
    private ReviewService reviewService;

    @Mock
    private WhatsAppService whatsAppService;


    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";
    private static final String PRODUCTS_CACHE_NAME = "productsCache";
    private static final String PRODUCT_CACHE_KEY = "listAllProduct";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void placeOrder_Success() {
        OrderRequest orderRequest = getOrderRequest();
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_LoyaltyPointsToRedeem_Is_Not_Null() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setLoyaltyPointsToRedeem(5.0);
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(loyaltyPointsService.redeemLoyaltyPoints(any(),any(),any())).thenReturn(new SuccessResponse());
        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_Cache_Is_Not_Null() {
        OrderRequest orderRequest = getOrderRequest();
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Not_Null() {
        OrderRequest orderRequest = getOrderRequest();
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY,ProductResponse.class)).thenReturn(cachedProductResponse);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_OrderItemRequest_Quantity_Is_Less_Than_ColorQuantity() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.getOrderItemRequests().getFirst().setQuantity(2);
        orderRequest.setCustomizationCartResponse(getCustomizationCartResponse());
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY,ProductResponse.class)).thenReturn(cachedProductResponse);
        when(cache.get(PRODUCT_CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        when(userCustomizationRepository.save(any(UserCustomizationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderServiceMappingHelper.extractFabricDetails(any())).thenReturn(getFabric());
        when(cartRepository.deleteItemsByDetails(any(), any(), any(), any(), any(), any())).thenReturn(1L);
        when(cartRepository.findByUserId(anyString())).thenReturn(getCartEntity());
        when(serviceImplementation.isSameProduct(any(), any())).thenReturn(true);

        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Not_Null_With_OrderItem_Null() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(null);
        orderRequest.setCustomizationCartResponse(getCustomizationCartResponse());
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        when(userCustomizationRepository.save(any(UserCustomizationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderServiceMappingHelper.extractFabricDetails(any())).thenReturn(getFabric());
        when(cartRepository.findByUserId(anyString())).thenReturn(getCartEntity());
        when(serviceImplementation.isSameProduct(any(), any())).thenReturn(true);

        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Not_Null_With_CustomizedAddDataResponse_Is_Null() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(null);
       List<CustomizationCartResponse> cartResponse = getCustomizationCartResponse();
        cartResponse.getFirst().setCustomizedAddDataResponse(null);
        orderRequest.setCustomizationCartResponse(cartResponse);
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        when(userCustomizationRepository.save(any(UserCustomizationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderServiceMappingHelper.extractFabricDetails(any())).thenReturn(getFabric());
        when(cartRepository.findByUserId(anyString())).thenReturn(getCartEntity());
        when(serviceImplementation.isSameProduct(any(), any())).thenReturn(true);

        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Not_Null_When_OrderItem_Empty() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(new ArrayList<>());
        orderRequest.setCustomizationCartResponse(getCustomizationCartResponse());
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        when(userCustomizationRepository.save(any(UserCustomizationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderServiceMappingHelper.extractFabricDetails(any())).thenReturn(getFabric());
        when(cartRepository.findByUserId(anyString())).thenReturn(getCartEntity());
        when(serviceImplementation.isSameProduct(any(), any())).thenReturn(true);

        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Not_Null_When_OrderItem_Not_Null() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(getListOrderItemRequest());
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        when(userCustomizationRepository.save(any(UserCustomizationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderServiceMappingHelper.extractFabricDetails(any())).thenReturn(getFabric());

        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CustomizationCartEntity_Is_Empty() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(getListOrderItemRequest());
        orderRequest.setCustomizationCartResponse(List.of());
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        ProductResponse cachedProductResponse = new ProductResponse();
        cachedProductResponse.setCategoryResponses(List.of(getCategoryResponse()));
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        when(cacheManager.getCache(PRODUCTS_CACHE_NAME)).thenReturn(cache);
        when(cache.get(PRODUCT_CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(cachedProductResponse);
        when(userCustomizationRepository.save(any(UserCustomizationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.save(any(OrderItemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderServiceMappingHelper.extractFabricDetails(any())).thenReturn(getFabric());

        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Not_Null_With_OrderItem_Empty() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(new ArrayList<>());
        orderRequest.setCustomizationCartResponse(List.of());
        OrderItemNotFoundException exception = assertThrows(OrderItemNotFoundException.class, ()->orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.ORDER_ITEM_NOT_FOUND, exception.getMessage());
    }

    @Test
    void placeOrder_Success_When_CachedProductResponse_Is_Null_With_OrderItem_Empty() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(new ArrayList<>());
        OrderItemNotFoundException exception = assertThrows(OrderItemNotFoundException.class, ()->orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.ORDER_ITEM_NOT_FOUND, exception.getMessage());
    }

    private Fabric getFabric() {
        Fabric fabric = new Fabric();
        fabric.setFabricId("FabricId1");
        fabric.setFabricColorCode("#000000");
        fabric.setFabricDescription("cotton");
        fabric.setImageUrl(List.of("FabricImg1", "FabricImg2"));
        fabric.setFabricColor("white");
        fabric.setFabricPrice(100.0);
        fabric.setProductOfferPercentage(5.0);
        return fabric;
    }

    private List<CustomizationCartResponse> getCustomizationCartResponse() {
        List<CustomizationCartResponse> customizationCartResponses = new ArrayList<>();
        CustomizationCartResponse cartResponse = new CustomizationCartResponse();
        cartResponse.setCustomizedCartItemId("c1");
        cartResponse.setColor("black");
        cartResponse.setPrice(1499.99);
        cartResponse.setQuantity(1);
        cartResponse.setCustomizedAddDataResponse(getCustomizedAddResponse());
        customizationCartResponses.add(cartResponse);
        return customizationCartResponses;
    }

    private CustomizedAddDataResponse getCustomizedAddResponse() {
        CustomizedAddDataResponse response = new CustomizedAddDataResponse();
        response.setPantType("formal");
        response.setTrueWaistMeasurement(30);
        response.setPantInSeamLength(28);
        response.setPantOutSeamLength(38);
        response.setFitType("slimfit");
        response.setRiseType("midRaise");
        response.setFrontPocketType("normal");
        response.setBackPocketType("normal");
        response.setFrontButtonType("round");
        response.setBackButtonType("round");
        response.setPantPleatType("pleat");
        response.setFlyType("butterfly");
        response.setPantCuffsType("ribbon");
        String fabricDetailsJson = "{ \"material\": \"cotton\", \"weight\": \"light\", \"color\": \"blue\" }";
        JsonNode fabricDetails = objectMapper.valueToTree(fabricDetailsJson);
        response.setFabric(fabricDetails);
        return response;
    }

    private CategoryResponse getCategoryResponse() {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setCategoryName("Category 1");
        categoryResponse.setCategoryDescription("category Description");
        categoryResponse.setCategoryName("Pants");
        categoryResponse.setSubCategoryResponses(List.of(getSubCategoryResponse()));
        return categoryResponse;
    }

    private SubCategoryResponse getSubCategoryResponse() {
        SubCategoryResponse subCategoryResponse = new SubCategoryResponse();
        subCategoryResponse.setSubCategoryName("Sub Category 1");
        subCategoryResponse.setSubCategoryDescription("Sub Category Description");
        subCategoryResponse.setProductDataResponses(List.of(getProductDataResponse()));
        subCategoryResponse.setSubCategoryId("Cat1");
        return subCategoryResponse;
    }

    private ProductDataResponse getProductDataResponse() {
        ProductDataResponse productDataResponse = new ProductDataResponse();
        productDataResponse.setProductId("ID1");
        productDataResponse.setProductReviewResponse(new ProductReviewResponse());
        productDataResponse.setStockQuantityResponseList(getListStockQuantityResponse());
        return productDataResponse;
    }

    private List<StockQuantityResponse> getListStockQuantityResponse() {
        List<StockQuantityResponse> stockQuantityResponseList = new ArrayList<>();
        StockQuantityResponse response = new StockQuantityResponse();
        response.setSize(1);
        response.setProductPrice(1499.99);
        response.setColorQuantityResponses(getListColorQuantityResponse());
        stockQuantityResponseList.add(response);
        return stockQuantityResponseList;
    }

    private List<ColorQuantityResponse> getListColorQuantityResponse() {
        List<ColorQuantityResponse> colorQuantityResponsesList = new ArrayList<>();
        ColorQuantityResponse response = new ColorQuantityResponse();
        response.setQuantity(1);
        response.setColor("black");
        response.setColorCode("#fffff");
        response.setImageUrls(new HashMap<>());
        colorQuantityResponsesList.add(response);
        return colorQuantityResponsesList;
    }

    @Test
    void placeOrder_Exception_AddressEntity_Not_Found() {
        OrderRequest orderRequest = getOrderRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(null);
        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, () -> orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.ADDRESS_NOT_FOUND, exception.getMessage());
    }

    @Test
    void placeOrder_Success_When_PaymentMethod_IS_COD() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setPaymentMethod(Constants.COD);
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        PlaceAnOrderResponse placeAnOrderResponse = orderServiceImplementation.placeAnOrder(orderRequest);
        assertEquals(Constants.ORDER_PLACED_SUCCESSFULLY, placeAnOrderResponse.getMessage());
    }


    @Test
    void placeOrder_OrderPlacingException_When_ColorQuantity_Is_LessThan_OrderItemRequestQuantity() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.getOrderItemRequests().getFirst().setQuantity(10);
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        OrderPlacingException exception = assertThrows(OrderPlacingException.class, () -> orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals("Insufficient stock for color: " + orderRequest.getOrderItemRequests().getFirst().getColor() + ", size: " + orderRequest.getOrderItemRequests().getFirst().getSize() + ", product: " + optionalProduct.get().getProductName(), exception.getMessage());
    }


    @Test
    void placeOrder_OrderPlacingException_When_Size_Is_Not_In_Stock() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.setOrderItemRequests(getListOfNonMatchingOrderItemRequest());
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        OrderPlacingException exception = assertThrows(OrderPlacingException.class, () -> orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.STOCK_NOT_FOUND + orderRequest.getOrderItemRequests().getFirst().getSize(), exception.getMessage());
    }

    @Test
    void placeOrder_OrderPlacingException_When_Color_Is_Not_In_Stock() {
        OrderRequest orderRequest = getOrderRequest();
        orderRequest.getOrderItemRequests().getFirst().setColor("white");
        UserEntity userEntity = getUserEntity();
        Optional<Product> optionalProduct = getProduct();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(optionalProduct);
        OrderPlacingException exception = assertThrows(OrderPlacingException.class, () -> orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.COLOR_NOT_FOUND + orderRequest.getOrderItemRequests().getFirst().getColor(), exception.getMessage());
    }

    @Test
    void placeOrder_OrderPlacingException_When_Product_Not_Found() {
        OrderRequest orderRequest = getOrderRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(new AddressEntity());
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        OrderPlacingException exception = assertThrows(OrderPlacingException.class, () -> orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.PRODUCT_NOT_FOUND + orderRequest.getOrderItemRequests().getFirst().getProductId(), exception.getMessage());
    }

    @Test
    void placeOrder_OrderPlacingException_When_User_Not_Found() {
        OrderRequest orderRequest = getOrderRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> orderServiceImplementation.placeAnOrder(orderRequest));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCancelOrder_When_PaymentIsNotSuccess() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.FAILED);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.NOT_REQUESTED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_RedeemPoints_GreaterThan_Zero() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setRedeemedLoyaltyPoints(5.0);
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.FAILED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(loyaltyPointsService.refundPointsForCancelledOrderItem(any(),any(),any())).thenReturn(new SuccessResponse());
        assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_PaymentIsSuccess_RefundAlreadyRequested() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.REQUESTED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        RefundProcessException exception = assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.REFUND_ALREADY_REQUESTED,exception.getMessage());
    }

    @Test
    void testCancelOrder_Exception_When_PaymentIsSuccess_RefundAlreadyInitiated() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.INITIATED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_PaymentIsSuccess_RefundAlreadyCompleted() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.COMPLETED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_PaymentIsSuccess_RefundAlreadyFailed() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.FAILED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_PaymentIsSuccess_UnknownRefundStatus() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.NOT_APPLICABLE);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        assertThrows(RefundProcessException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_PaymentIsSuccess_NoActionIsEnableForRefund() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        cancelOrderRequest.setIsRefund(false);
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.NOT_REQUESTED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        CancelOrderResponse cancelOrderResponse = orderServiceImplementation.cancelOrder(cancelOrderRequest);
        assertEquals(Constants.ORDER_CANCELED_SUCCESSFULLY, cancelOrderResponse.getMessage());
    }

    @Test
    void testCancelOrder_Successful() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.NOT_REQUESTED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(paymentServiceImpl.refundProcess(any(),any())).thenReturn(new SuccessResponse(Constants.REFUND_SUCCESSFUL, HttpStatus.OK.value()));
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        CancelOrderResponse response = orderServiceImplementation.cancelOrder(cancelOrderRequest);
        assertEquals(Constants.ORDER_CANCELED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void testCancelOrder_When_RefundProcess_ThrowsException() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.NOT_REQUESTED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        when(paymentServiceImpl.refundProcess(any(),any())).thenThrow(new RefundProcessException(String.format(Constants.RAZORPAY_REFUND_FAILED, paymentEntity.getRazorPayPaymentId())));
        when(refundRepository.findByPaymentEntityAndOrderItemEntity(any(), any())).thenReturn(paymentEntity.getRefundEntities().getFirst());
        assertThrows(RefundProcessException.class, ()->orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void testCancelOrder_When_OrderItemId_IsNotPresentInRequest() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        cancelOrderRequest.setOrderItemId(null);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenThrow(new OrderItemNotFoundException(Constants.ORDER_ITEM_NOT_FOUND));
        when(orderRepository.findByOrderIdAndUserId(anyString(),anyString())).thenReturn(getOrderEntity());
        assertThrows(OrderItemNotFoundException.class, ()->orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }

    @Test
    void cancelOrder_Success_When_RefundStatus_Is_NotRequested_And_IsRefunded() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.NOT_REQUESTED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(paymentEntity);
        assertThrows(RuntimeException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
    }


    @Test
    void cancelOrder_Exception_When_PaymentEntity_Is_Null() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByPaymentId(anyString())).thenReturn(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.PAYMENT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void cancelOrder_Success_When_Payment_Method_Is_COD() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        cancelOrderRequest.setOrderId("123");
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getPaymentEntity().setPaymentMethod(Constants.COD);
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByPaymentId(any())).thenReturn(paymentEntity);
        CancelOrderResponse cancelOrderResponse = orderServiceImplementation.cancelOrder(cancelOrderRequest);
        assertEquals(cancelOrderResponse.getOrderId(), cancelOrderRequest.getOrderId());
    }

    @Test
    void cancelOrder_Exception_When_OrderDate_Exceeds() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderDate(LocalDateTime.now().minusDays(8));
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        OrderCancellationException exception = assertThrows(OrderCancellationException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.ORDER_CANNOT_BE_CANCELED_AFTER_7_DAYS, exception.getMessage());
    }

    @Test
    void cancelOrder_Exception_When_OrderStatus_Is_Cancelled() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderStatus(OrderStatus.CANCELED);
        orderEntity.getOrderItemEntities().getFirst().setOrderStatus(OrderStatus.CANCELED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        OrderCancellationException exception = assertThrows(OrderCancellationException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.ORDER_CANNOT_BE_CANCELED , exception.getMessage());
    }

    @Test
    void cancelOrder_Exception_When_DeliveryStatus_Is_Shipped() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.SHIPPED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        OrderCancellationException exception = assertThrows(OrderCancellationException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.ORDER_CANNOT_BE_CANCELED , exception.getMessage());
    }

    @Test
    void cancelOrder_Exception_When_DeliveryStatus_Is_Delivered() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        OrderCancellationException exception = assertThrows(OrderCancellationException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.ORDER_CANNOT_BE_CANCELED , exception.getMessage());
    }

    @Test
    void cancelOrder_Exception_When_DeliveryStatus_Is_Returned() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.RETURNED);

        OrderItemEntity orderItemEntity = orderEntity.getOrderItemEntities().getFirst();
        orderItemEntity.setDeliveryStatus(DeliveryStatus.RETURNED);

        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderItemEntity);
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        OrderCancellationException exception = assertThrows(OrderCancellationException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.ORDER_CANNOT_BE_CANCELED , exception.getMessage());
    }

    @Test
    void cancelOrder_Exception_When_OrderEntity_Is_Null() {
        CancelOrderRequest cancelOrderRequest = getCancelOrderRequest();
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(null);
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> orderServiceImplementation.cancelOrder(cancelOrderRequest));
        assertEquals(Constants.ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void returnOrder_Success_When_RequestType_IsRefund() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        PaymentEntity paymentEntity = getPaymentEntity(orderEntity);
        paymentEntity.getRefundEntities().getFirst().setRefundStatus(RefundStatus.INITIATED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(paymentRepository.findByOrderEntityOrderId(anyString())).thenReturn(Optional.of(paymentEntity));
        when(paymentServiceImpl.refundProcess(any(), any())).thenReturn(new SuccessResponse(Constants.REFUND_SUCCESSFUL, HttpStatus.OK.value()));
        ReturnOrderResponse returnOrderResponse = orderServiceImplementation.returnOrder(request);
        assertEquals(Constants.PRODUCT_RETURN_PENDING_MSG, returnOrderResponse.getOrderRefundResponse().getMessage());
    }

    @Test
    void testReturnOrder_When_OrderItem_IsAlreadyReturned() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderEntity.getOrderItemEntities().getFirst().setReturnStatus(ReturnStatus.REFUND_COMPLETED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        InvalidReturnOperationException exception = assertThrows(InvalidReturnOperationException.class,
                ()-> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.ORDER_ITEM_ALREADY_REFUNDED, exception.getMessage());
    }

    @Test
    void testReturnOrder_Success_When_RequestType_IsExchange() {
        ReturnRequest request = getReturnRequest();
        request.setReturnType("exchange");
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        ReturnOrderResponse response = orderServiceImplementation.returnOrder(request);
        assertEquals(Constants.EXCHANGE_SUCCESSFUL, response.getOrderExchangeResponse().getMessage());
    }

    @Test
    void testReturnOrder_Success_When_RequestType_IsExchange_WhenAdditionalAmountRequired() {
        ReturnRequest request = getReturnRequest();
        request.setReturnType("exchange");
        request.getReplacementOrderItemRequest().setTotalAmount(200.00);
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        ReturnOrderResponse response = orderServiceImplementation.returnOrder(request);
        assertEquals(Constants.ADDITIONAL_PAYMENT_REQUIRED, response.getOrderExchangeResponse().getMessage());
    }

    @Test
    void testReturnOrder_Success_When_RequestType_Is_Not_Exchange_Or_Refund() {
        ReturnRequest request = getReturnRequest();
        request.setReturnType("");
        OrderEntity orderEntity = getOrderEntity();
        OrderItemEntity orderItem = orderEntity.getOrderItemEntities().getFirst();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderItem);
        ReturnOrderResponse response = orderServiceImplementation.returnOrder(request);
        assertNotNull(response);
    }

    @Test
    void testReturnOrder_When_DeliveryDate_AlreadyExceeded() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        LocalDateTime deliveryDate = LocalDateTime.parse("2024-11-21 18:30:21.826958", formatter);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryDate(deliveryDate);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderEntity.getOrderItemEntities().getFirst());
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        ReturnException exception = assertThrows(ReturnException.class, () -> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.RETURN_PERIOD_EXPIRED, exception.getMessage());
    }

    @Test
    void returnOrder_Exception_When_OrderStatus_Is_Returned() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderStatus(OrderStatus.RETURNED);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        ReturnException exception = assertThrows(ReturnException.class, () -> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.ORDER_ALREADY_RETURNED, exception.getMessage());
    }

    @Test
    void returnOrder_Exception_When_OrderStatus_And_DeliveryStatus_Are_Delivered() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.PENDING);
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        InvalidReturnOperationException exception = assertThrows(InvalidReturnOperationException.class, () -> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.ORDER_NOT_ELIGIBLE_FOR_RETURN, exception.getMessage());
    }

    @Test
    void returnOrder_Exception_When_RefundStatus_Is_Exchange_Confirmed() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderStatus(OrderStatus.DELIVERED);
        OrderItemEntity orderItem = orderEntity.getOrderItemEntities().getFirst();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItem.setReturnStatus(ReturnStatus.EXCHANGED_COMPLETED);
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderItem);
        InvalidReturnOperationException exception = assertThrows(InvalidReturnOperationException.class, () -> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.ALREADY_EXCHANGED, exception.getMessage());
    }

    @Test
    void returnOrder_Exception_When_RefundStatus_Is_Exchange() {
        ReturnRequest request = getReturnRequest();
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderStatus(OrderStatus.DELIVERED);
        OrderItemEntity orderItem = orderEntity.getOrderItemEntities().getFirst();
        orderItem.setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderItem.setReturnStatus(ReturnStatus.EXCHANGE);
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(orderEntity);
        when(orderServiceMappingHelper.getOrderItemEntity(any(),any())).thenReturn(orderItem);
        InvalidReturnOperationException exception = assertThrows(InvalidReturnOperationException.class, () -> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.CANNOT_EXCHANGE_MULTIPLE_ITEMS, exception.getMessage());
    }
    @Test
    void returnOrder_Exception_When_OrderEntity_Is_Null() {
        ReturnRequest request = getReturnRequest();
        when(orderRepository.findByOrderIdAndUserId(anyString(), anyString())).thenReturn(null);
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> orderServiceImplementation.returnOrder(request));
        assertEquals(Constants.ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getAllOrders_Success_When_OrderStatus_Is_Canceled_And_DeliveryStatus_Is_Delivered() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderStatus(OrderStatus.CANCELED);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(orderEntity));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(orderEntity)));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_DeliveryStatus_Is_Null() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(null);
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(orderEntity));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(orderEntity)));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_NewOptionalProduct_Is_Present() {
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(getOrderEntity()));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        Optional<Product> product = getProduct();
        product.ifPresent(value -> value.setProductImages(getImageMap()));
        when(productRepository.findByProductId(any())).thenReturn(product);
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(optionalOrderEntities.get().getFirst())));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_IsEligibleForBuyAgain_Is_True() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().minusDays(8));
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(orderEntity));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(productRepository.findByProductId(any())).thenReturn(getProduct());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(orderEntity)));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_IsEligibleForBuyAgain_Is_False() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setReturnDaysPolicy(7);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryDate(LocalDateTime.now().minusDays(8));
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(orderEntity));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(productRepository.findByProductId(any())).thenReturn(getProduct());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(orderEntity)));
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(),any())).thenReturn(new AddressEntity());
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_IsEligibleForBuyAgain_Is_False_DeliveryDate_Is_Null() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setDeliveryDate(null);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderEntity.getOrderItemEntities().getFirst().setUserCustomizationEntity(new UserCustomizationEntity());
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(orderEntity));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(productRepository.findByProductId(any())).thenReturn(getProduct());
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(),any())).thenReturn(new AddressEntity());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(orderEntity)));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_IsEligibleForBuyAgain_Is_False_ReturnDaysPolicy_Is_Null() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setReturnDaysPolicy(null);
        orderEntity.getOrderItemEntities().getFirst().setDeliveryStatus(DeliveryStatus.DELIVERED);
        orderEntity.getOrderItemEntities().getFirst().setUserCustomizationEntity(new UserCustomizationEntity());
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(orderEntity));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(productRepository.findByProductId(any())).thenReturn(getProduct());
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(),any())).thenReturn(new AddressEntity());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(orderEntity)));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }


    @Test
    void getAllOrders_Success_When_OptionalProduct_Is_Present() {
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(getOrderEntity()));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(productRepository.findByProductId(any())).thenReturn(getProduct());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(optionalOrderEntities.get().getFirst())));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_OptionalProduct_Is_Absent() {
        Optional<List<OrderEntity>> optionalOrderEntities = Optional.of(List.of(getOrderEntity()));
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(optionalOrderEntities);
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(optionalOrderEntities.get().getFirst())));
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void getAllOrders_Success_When_OrderEntities_Is_Empty() {
        when(orderRepository.findByUserEntityUserId(any())).thenReturn(Optional.empty());
        GetAllOrdersResponse orderResponses = orderServiceImplementation.getAllOrders(ENCRYPTED_USER_ID);
        assertNotNull(orderResponses);
    }

    @Test
    void testGetOrderDetailsByOrderId_Success() {
        when(orderRepository.findById(any())).thenReturn(Optional.of(getOrderEntity()));
        when(productRepository.findByProductId(anyString())).thenReturn(getProduct());
        when(addressRepository.findById(any())).thenReturn(getAddressEntity());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(getOrderEntity())));
        OrderSummaryResponse response = orderServiceImplementation.getOrderDetailsByOrderId("order123","user123");
        assertEquals("10",response.getAddressResponse().getAddressId());
    }


    @Test
    void testGetOrderDetailsByOrderId_When_Product_IsEmpty() {
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.getOrderItemEntities().getFirst().setUserCustomizationEntity(new UserCustomizationEntity());
        when(orderRepository.findById(any())).thenReturn(Optional.of(orderEntity));
        when(productRepository.findByProductId(anyString())).thenReturn(Optional.empty());
        when(addressRepository.findById(any())).thenReturn(getAddressEntity());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(getOrderEntity())));
        OrderSummaryResponse response = orderServiceImplementation.getOrderDetailsByOrderId("order123","user123");
        assertEquals("10",response.getAddressResponse().getAddressId());
    }

    @Test
    void testGetOrderDetailsByOrderId_When_Address_IsEmpty() {
        when(orderRepository.findById(any())).thenReturn(Optional.of(getOrderEntity()));
        when(productRepository.findByProductId(anyString())).thenReturn(getProduct());
        when(addressRepository.findById(any())).thenReturn(Optional.empty());
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getPaymentEntity(getOrderEntity())));
        AddressNotFoundException exception = assertThrows(AddressNotFoundException.class, ()-> orderServiceImplementation.getOrderDetailsByOrderId("order123","user123"));
        assertEquals(Constants.ADDRESS_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testGetOrderDetailsByOrderId_When_Payment_IsEmpty() {
        when(orderRepository.findById(any())).thenReturn(Optional.of(getOrderEntity()));
        when(productRepository.findByProductId(anyString())).thenReturn(getProduct());
        when(addressRepository.findById(any())).thenReturn(getAddressEntity());
        when(paymentRepository.findById(any())).thenReturn(Optional.empty());
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, ()-> orderServiceImplementation.getOrderDetailsByOrderId("order123","user123"));
        assertEquals(Constants.PAYMENT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testGetOrderDetailsByOrderId_When_OrderEntity_IsEmpty() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, ()-> orderServiceImplementation.getOrderDetailsByOrderId("order123","user123"));
        assertEquals(Constants.ORDER_NOT_FOUND + "order123", exception.getMessage());
    }

    private Optional<AddressEntity> getAddressEntity() {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddressId("10");
        addressEntity.setLandMark("near Government high school");
        addressEntity.setTypeOfAddress("home");
        addressEntity.setPinCode("560064");
        addressEntity.setStreetName("kogilu");
        addressEntity.setStateName("karnataka");
        addressEntity.setUserEntity(getUserEntity());
        addressEntity.setOrderEntities(List.of(new OrderEntity()));
        addressEntity.setBuildingName("ramanashree");
        addressEntity.setCityName("yelahanka");
        addressEntity.setCountryName("India");
        addressEntity.setFirstName("jay");
        addressEntity.setLastName("prakash");
        addressEntity.setPhoneNumber("1234567890");
        return Optional.of(addressEntity);
    }

    private PaymentEntity getPaymentEntity(OrderEntity orderEntity) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderEntity(orderEntity);
        paymentEntity.setPaymentDate(LocalDateTime.now());
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentMethod(Constants.RAZORPAY);
        paymentEntity.setUserId("1");
        paymentEntity.setRazorPayOrderId("razorpay123");
        paymentEntity.setPaymentId("pay123");
        paymentEntity.setRazorPayPaymentId("rpayment123");
        paymentEntity.setTotalAmount(150.0);
        paymentEntity.setRefundEntities(getListRefundEntities(paymentEntity,paymentEntity.getOrderEntity().getOrderItemEntities().getFirst()));
        return paymentEntity;
    }

    private List<RefundEntity> getListRefundEntities(PaymentEntity paymentEntity, OrderItemEntity first) {
        List<RefundEntity> refundEntities = new ArrayList<>();
        RefundEntity refundEntity = getRefundEntity(paymentEntity, first);
        refundEntities.add(refundEntity);
        return refundEntities;
    }

    private RefundEntity getRefundEntity(PaymentEntity paymentEntity, OrderItemEntity first) {
        RefundEntity refundEntity = new RefundEntity();
        refundEntity.setRefundId("r123");
        refundEntity.setRefundDate(LocalDateTime.now());
        refundEntity.setRefundStatus(RefundStatus.NOT_REQUESTED);
        refundEntity.setRefundAmount(10.0);
        refundEntity.setRefundIdOrPayoutId("pout_35gfev2j21njk");
        refundEntity.setRefundType(RefundType.PRODUCT);
        refundEntity.setPaymentEntity(paymentEntity);
        refundEntity.setOrderItemEntity(first);
        return refundEntity;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId("123");
        orderEntity.setUserEntity(new UserEntity());
        orderEntity.setOrderDate(LocalDateTime.now());
        orderEntity.setOrderStatus(OrderStatus.DELIVERED);
        orderEntity.setPaymentMethod(Constants.RAZORPAY);
        orderEntity.setAddressEntity(new AddressEntity());
        orderEntity.setTotalAmount(150.0);
        List<OrderItemEntity> orderItemEntities = getListOfOrderItemEntities(orderEntity);
        orderEntity.setOrderItemEntities(orderItemEntities);
        orderEntity.setPaymentEntity(getPaymentEntity(orderEntity));
        return orderEntity;
    }

    private List<OrderItemEntity> getListOfOrderItemEntities(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderEntity(orderEntity);
        orderItemEntity.setOrderItemId("123");
        orderItemEntity.setProductId("P1");
        orderItemEntity.setUnitPrice(100.0);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setReturnDaysPolicy(7);
        orderItemEntity.setTotalAmount(150.0);
        orderItemEntity.setProductOfferPercentage(20.0);
        orderItemEntity.setOrderStatus(OrderStatus.CONFIRMED);
        orderItemEntity.setDeliveryStatus(DeliveryStatus.ORDER_CONFIRMED);
        orderItemEntity.setReturnStatus(ReturnStatus.NOT_RETURNED);
        orderItemEntity.setDeliveryDate(LocalDateTime.now().plusDays(5));
        orderItemEntities.add(orderItemEntity);
        return orderItemEntities;
    }

    private Optional<Product> getProduct() {
        Product product = new Product();
        product.setProductName("shirt");
        product.setProductDescription("formal white shirt");
        product.setProductPattern("plain");
        product.setProductStatus("Pending");
        product.setProductMaterialType("cotton");
        product.setProductOfferPercentage(5.0);
        product.setProductImages(getImageMap());
        product.setStockQuantities(getListStockQuantity(product));
        product.setSubCategory(new SubCategory());
        product.setVideoUrl("video.com");
        return Optional.of(product);
    }


    private List<StockQuantity> getListStockQuantity(Product product) {
        List<StockQuantity> stockQuantityList = new ArrayList<>();
        StockQuantity stockQuantity = new StockQuantity();

        stockQuantity.setProduct(product);

        stockQuantity.setSize(32);
        stockQuantity.setColorQuantities(getListColorQuantity());

        stockQuantityList.add(stockQuantity);
        return stockQuantityList;
    }

    private List<ColorQuantity> getListColorQuantity() {
        List<ColorQuantity> colorQuantityList = new ArrayList<>();
        ColorQuantity colorQuantity = new ColorQuantity();
        colorQuantity.setColorQuantityId("CQ1");
        colorQuantity.setColor("black");
        colorQuantity.setColorCode("#000000");
        colorQuantity.setQuantity(5);
        colorQuantityList.add(colorQuantity);
        return colorQuantityList;
    }

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("1");
        userEntity.setOrderEntities(new ArrayList<>());
        userEntity.setUserCreatedAt(LocalDateTime.now());
        userEntity.setReferredReferralCode("123456");
        userEntity.setReferralCode("123456");
        userEntity.setUserType("customer");
        userEntity.setAuthenticationSource("google");
        userEntity.setEmailAddressVerified(true);
        userEntity.setFirstName("jay");
        userEntity.setEmailAddress("jay@gmail.com");
        userEntity.setLastName("doe");
        userEntity.setPhoneNumber("1234567890");
        userEntity.setPhoneNumberVerified(true);
        userEntity.setAddressEntityList(List.of(new AddressEntity()));
        userEntity.setCardEntities(List.of(new CardEntity()));
        return userEntity;
    }

    private OrderRequest getOrderRequest() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId("123");
        orderRequest.setTotalAmount(100.0);
        orderRequest.setPaymentMethod(Constants.RAZORPAY);
        orderRequest.setOrderItemRequests(getListOrderItemRequest());
        orderRequest.setShippingAddress(new AddressRequest());
        return orderRequest;
    }

    private List<OrderItemRequest> getListOrderItemRequest() {
        List<OrderItemRequest> orderItemRequests = new ArrayList<>();
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setQuantity(1);
        orderItemRequest.setProductId("ID1");
        orderItemRequest.setTotalAmount(100.0);
        orderItemRequest.setColor("black");
        orderItemRequest.setSize(32);
        orderItemRequest.setOfferPercentage(10.0);
        orderItemRequests.add(orderItemRequest);
        return orderItemRequests;
    }

    private List<OrderItemRequest> getListOfNonMatchingOrderItemRequest() {
        List<OrderItemRequest> orderItemRequests = new ArrayList<>();
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setQuantity(1);
        orderItemRequest.setProductId("ID1");
        orderItemRequest.setTotalAmount(100.0);
        orderItemRequest.setColor("white");
        orderItemRequest.setSize(30);
        orderItemRequests.add(orderItemRequest);
        return orderItemRequests;
    }

    private CancelOrderRequest getCancelOrderRequest() {
        CancelOrderRequest cancelOrderRequest = new CancelOrderRequest();
        cancelOrderRequest.setUserId("123");
        cancelOrderRequest.setOrderId("456");
        cancelOrderRequest.setOrderItemId("123");
        cancelOrderRequest.setReason("Not Worth It");
        cancelOrderRequest.setIsRefund(true);
        return cancelOrderRequest;
    }

    private ReturnRequest getReturnRequest() {
        ReturnRequest returnRequest = new ReturnRequest();
        returnRequest.setOrderId("123456");
        returnRequest.setUserId("123");
        returnRequest.setOrderItemId("123");
        returnRequest.setReason("Not Worth It");
        returnRequest.setReturnType("refund");
        returnRequest.setReplacementOrderItemRequest(getReplacementOrderItemRequest());
        return returnRequest;
    }

    private OrderItemRequest getReplacementOrderItemRequest() {
        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setQuantity(1);
        orderItemRequest.setProductId("ID1");
        orderItemRequest.setTotalAmount(100.0);
        orderItemRequest.setColor("white");
        orderItemRequest.setSize(30);
        return orderItemRequest;
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

}