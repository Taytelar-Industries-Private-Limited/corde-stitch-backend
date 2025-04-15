package com.cordestitch.serviceimplementation.customization;


import com.cordestitch.service.serviceimplementation.customization.UserCustomizationServiceImplementation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.cart.CartEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.entity.cart.CustomizedCartItemEntity;
import com.cordestitch.entity.customization.Fabric;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.cart.CartItemNotFoundException;
import com.cordestitch.exception.customization.ConvertFromJsonException;
import com.cordestitch.exception.customization.FabricNotFoundException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.cart.CartRepository;
import com.cordestitch.repository.customization.UserCustomizationRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.customization.usercustomization.CustomizedAddDataRequest;
import com.cordestitch.request.customization.usercustomization.CustomizedCartItemRequest;
import com.cordestitch.request.customization.usercustomization.UpdateCartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserCustomizationServiceImplementationTest {
    @InjectMocks
    private UserCustomizationServiceImplementation userCustomizationServiceImplementation;

    @Mock
    private UserCustomizationRepository userCustomizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private Generator generator;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addCustomizedDataToCart_Success() throws Exception{
        CustomizedAddDataRequest customizedAddDataRequest = getCustomizedDataRequest();
        CustomizedCartItemRequest customizedCartItemRequest = getCustomizedCartItemRequest();
        String userId = "user1";
        when(userRepository.findUserByUserId(userId)).thenReturn(getUserEntity());
        SuccessResponse response = userCustomizationServiceImplementation.addCustomizedDataToCart(customizedAddDataRequest,customizedCartItemRequest,userId);
        assertNotNull(response);
        assertEquals(Constants.ADDED_TO_CART_SUCCESSFULLY,response.getMessage());
    }

    @Test
    void addCustomizedDataToCart_Exception_User_Not_Found() throws Exception{
        CustomizedAddDataRequest customizedAddDataRequest = getCustomizedDataRequest();
        CustomizedCartItemRequest customizedCartItemRequest = getCustomizedCartItemRequest();
        String userId = "user1";
        when(userRepository.findUserByUserId(userId)).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->userCustomizationServiceImplementation.addCustomizedDataToCart(customizedAddDataRequest,customizedCartItemRequest,userId));
        assertEquals(Constants.USER_NOT_FOUND,exception.getMessage());
    }
    
    @Test
    void addToCart_Success() throws Exception{
        CustomizedCartItemRequest customizedCartItemRequest = getCustomizedCartItemRequest();
        CustomizedAddDataResponse customizedAddDataResponse = getCustomizedAddDataResponse();
        CartEntity cartEntity = getCartEntity();
        Fabric fabric =getFabric();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(objectMapper.treeToValue(customizedAddDataResponse.getFabric(), Fabric.class)).thenReturn(fabric);
        SuccessResponse response = userCustomizationServiceImplementation.addToCart(customizedCartItemRequest,customizedAddDataResponse, getUserEntity());
        assertEquals(Constants.ADDED_TO_CART_SUCCESSFULLY,response.getMessage());
    }

    @Test
    void addToCart_Exception_Fabric_Parse_Error() throws Exception{
        CustomizedCartItemRequest customizedCartItemRequest = getCustomizedCartItemRequest();
        CustomizedAddDataResponse customizedAddDataResponse = getCustomizedAddDataResponse();
        ObjectMapper mapper = new ObjectMapper();
        customizedAddDataResponse.setFabric(mapper.readTree("[{\"id\": 1, \"Description\": \"Denim\"}, {\"id\": 2, \"Description\": \"Cotton\"}]"));
        CartEntity cartEntity = getCartEntity();
        UserEntity userEntity = getUserEntity();
        when(cartRepository.findByUserId(any())).thenReturn(cartEntity);
        when(objectMapper.treeToValue(customizedAddDataResponse.getFabric(), Fabric.class)).thenThrow(new ConvertFromJsonException(Constants.FABRIC_PARSE_ERROR));
        FabricNotFoundException exception = assertThrows(FabricNotFoundException.class, () -> userCustomizationServiceImplementation.addToCart(customizedCartItemRequest, customizedAddDataResponse,userEntity));
        assertEquals(Constants.FABRIC_PARSE_ERROR,exception.getMessage());
    }

    @Test
    void deleteCustomizedCartItem(){
        String userId = "user1";
        String customizedCartItemId = "customizedCartItem1";
        when(cartRepository.findByUserId(userId)).thenReturn(getCartEntity());
        userCustomizationRepository.deleteById(any());
        SuccessResponse  response = userCustomizationServiceImplementation.deleteCustomizedCartItem(userId,customizedCartItemId);
        assertEquals(Constants.CART_ITEM_DELETED,response.getMessage());
    }

    @Test
    void deleteCustomizedCartItem_Exception_Cart_Item_Not_Found(){
        String userId = "user1";
        String customizedCartItemId = "customCart1";
        when(cartRepository.findByUserId(userId)).thenReturn(getCartEntity());
        userCustomizationRepository.deleteById(any());
        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, ()->userCustomizationServiceImplementation.deleteCustomizedCartItem(userId,customizedCartItemId));
        assertEquals(Constants.CART_ITEM_NOT_FOUND + customizedCartItemId,exception.getMessage());
    }

    @Test
    void deleteCustomizedCartItem_Exception_Cart_Not_Found(){
        String userId = "user1";
        String customizedCartItemId = "customCart1";
        when(cartRepository.findByUserId(userId)).thenReturn(null);
        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, ()->userCustomizationServiceImplementation.deleteCustomizedCartItem(userId,customizedCartItemId));
        assertEquals(Constants.CART_NOT_FOUND + userId,exception.getMessage());
    }

    @Test
    void updateCustomizedCartItem_Success(){
        UpdateCartItemRequest request = getUpdateCartItemRequest();
        when(cartRepository.findByUserId(any())).thenReturn(getCartEntity());
        SuccessResponse response = userCustomizationServiceImplementation.updateCustomizedCartItem(request);
        assertEquals(Constants.CART_ITEM_UPDATE,response.getMessage());
    }

    @Test
    void updateCustomizedCartItem_Success_When_Request_Is_EqualTO_CartEntity_CustomizedCartItemId(){
        UpdateCartItemRequest request = getUpdateCartItemRequest();
        request.setCustomizedCartItemId("customizedCartItem1");
        when(cartRepository.findByUserId(any())).thenReturn(getCartEntity());
        SuccessResponse response = userCustomizationServiceImplementation.updateCustomizedCartItem(request);
        assertEquals(Constants.CART_ITEM_UPDATE,response.getMessage());
    }

    @Test
    void updateCustomizedCartItem_Exception_Cart_Not_Found(){
        UpdateCartItemRequest request = getUpdateCartItemRequest();
        when(cartRepository.findByUserId(any())).thenReturn(null);
        CartItemNotFoundException exception = assertThrows(CartItemNotFoundException.class, ()->userCustomizationServiceImplementation.updateCustomizedCartItem(request));
        assertEquals(Constants.CART_NOT_FOUND + request.getUserId(),exception.getMessage());
    }

    private UpdateCartItemRequest getUpdateCartItemRequest() {
        UpdateCartItemRequest request=new UpdateCartItemRequest();
        request.setUserId("user1");
        request.setCustomizedCartItemId("customId12");
        request.setQuantity(2);
        return request;
    }

    private Fabric getFabric() {
        Fabric fabric = new Fabric();
        fabric.setFabricId("1");
        fabric.setFabricDescription("Denim");
        fabric.setFabricColor("brown");
        fabric.setFabricPrice(150.0);
        fabric.setFabricColorCode("#ffff00");
        fabric.setProductOfferPercentage(5.0);
        fabric.setImageUrl(List.of("img1.com","img2.com"));
        return fabric;
    }

    private CustomizedAddDataResponse getCustomizedAddDataResponse() throws Exception{
        CustomizedAddDataResponse customizedAddDataResponse = new CustomizedAddDataResponse();
        customizedAddDataResponse.setBackButtonType("customizedAddData1");
        customizedAddDataResponse.setFlyType("fly");
        customizedAddDataResponse.setFitType("slim fit");
        customizedAddDataResponse.setPantType("formal");
        customizedAddDataResponse.setRiseType("regular");
        customizedAddDataResponse.setBackButtonType("regular");
        customizedAddDataResponse.setFrontButtonType("round");
        customizedAddDataResponse.setPantCuffsType("regular");
        customizedAddDataResponse.setFrontPocketType("regular");
        customizedAddDataResponse.setPantCuffsType("regular");
        customizedAddDataResponse.setPantInSeamLength(28);
        customizedAddDataResponse.setPantOutSeamLength(30);
        customizedAddDataResponse.setPantPleatType("single");
        ObjectMapper mapper = new ObjectMapper();
        customizedAddDataResponse.setFabric(mapper.readTree("[{\"fabricId\": 1, \"fabricDescription\": \"Denim\"}, {\"fabricId\": 2, \"fabricDescription\": \"Cotton\"}]"));
        return customizedAddDataResponse;
    }

    private CartEntity getCartEntity() {
        CartEntity cartEntity = new CartEntity();
        cartEntity.setCartId("cart1");
        cartEntity.setUserId("user1");
        cartEntity.setCartItemEntityList(getCartItemEntity());
        cartEntity.setCustomizedCartItemList(getCustomizedCartItemEntityList());
        return cartEntity;
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

    private CustomizedCartItemRequest getCustomizedCartItemRequest() {
        CustomizedCartItemRequest request = new CustomizedCartItemRequest();
        request.setQuantity(2);
        return request;
    }

    private CustomizedAddDataRequest getCustomizedDataRequest() throws Exception {
        String fabricDetailsJson = "{ \"material\": \"cotton\", \"weight\": \"light\", \"color\": \"blue\" }";
        JsonNode fabricDetails = objectMapper.readTree(fabricDetailsJson);
        CustomizedAddDataRequest customizationRequest = new CustomizedAddDataRequest();
        customizationRequest.setFlyType("flyType1");
        customizationRequest.setFitType("fitType1");
        customizationRequest.setPantCuffsType("ribben");
        customizationRequest.setPantType("formal");
        customizationRequest.setRiseType("rise");
        customizationRequest.setPantInSeamLength(15);
        customizationRequest.setPantOutSeamLength(13);
        customizationRequest.setPantPleatType("single");
        customizationRequest.setTrueWaistMeasurement(30);
        customizationRequest.setFrontPocketType("box");
        customizationRequest.setBackPocketType("button");
        customizationRequest.setBackButtonType("round");
        customizationRequest.setFrontButtonType("hexagon");
        customizationRequest.setFabricDetails(fabricDetails);
        return customizationRequest;
    }


}