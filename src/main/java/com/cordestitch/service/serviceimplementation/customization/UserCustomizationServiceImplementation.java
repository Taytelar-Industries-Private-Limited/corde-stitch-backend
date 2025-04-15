package com.cordestitch.service.serviceimplementation.customization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.cart.CartEntity;
import com.cordestitch.entity.cart.CustomizedCartItemEntity;
import com.cordestitch.entity.customization.Fabric;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.cart.CartItemNotFoundException;
import com.cordestitch.exception.customization.ConvertFromJsonException;
import com.cordestitch.exception.customization.FabricNotFoundException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.cart.CartRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.customization.usercustomization.CustomizedAddDataRequest;
import com.cordestitch.request.customization.usercustomization.CustomizedCartItemRequest;
import com.cordestitch.request.customization.usercustomization.UpdateCartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import com.cordestitch.service.service.customization.UserCustomizationService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCustomizationServiceImplementation implements UserCustomizationService {

    private final UserRepository userRepository;

    private final CartRepository cartRepository;

    private final Generator generator;

    private final ObjectMapper objectMapper;

    @Override
    public SuccessResponse addCustomizedDataToCart(CustomizedAddDataRequest customizedDataRequest, CustomizedCartItemRequest customizedCartItemRequest, String userId) {

        log.info("Adding customized data to cart for user: {}", customizedDataRequest);
        UserEntity userEntity = userRepository.findUserByUserId(userId);
        if (isNull(userEntity)) {
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        CustomizedAddDataResponse customizedAddDataResponse = mapToCustomizedAddDataResponse(customizedDataRequest);

        return addToCart(customizedCartItemRequest, customizedAddDataResponse, userEntity);
    }

    @Override
    public SuccessResponse addToCart(CustomizedCartItemRequest request, CustomizedAddDataResponse customizedAddDataResponse, UserEntity userEntity) {
        log.info("Adding item to cart: {}", request);
        CartEntity cartEntity = cartRepository.findByUserId(userEntity.getUserId());

        if (isNull(cartEntity)) {
            CartEntity cart = new CartEntity();
            List<CustomizedCartItemEntity> itemList = new ArrayList<>();
            cart.setCartId(generator.generateId(Constants.CART_ID));
            cart.setUserId(userEntity.getUserId());
            itemList.add(getCustomizedCartItemEntity(request, customizedAddDataResponse));
            cart.setCustomizedCartItemList(itemList);
            cartRepository.save(cart);
        } else {
            List<CustomizedCartItemEntity> customizedCartItems = cartEntity.getCustomizedCartItemList();
            boolean productExists = false;

            for (CustomizedCartItemEntity existingCustomizedCartItem : customizedCartItems) {
                if (isSameProduct(existingCustomizedCartItem, customizedAddDataResponse)) {
                    existingCustomizedCartItem.setQuantity(existingCustomizedCartItem.getQuantity() + request.getQuantity());
                    productExists = true;
                    break;
                }
            }

            if (!productExists) {
                cartEntity.getCustomizedCartItemList().add(getCustomizedCartItemEntity(request, customizedAddDataResponse));
            }
            cartRepository.save(cartEntity);
        }
        return new SuccessResponse(Constants.ADDED_TO_CART_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse deleteCustomizedCartItem(String userId, String customizedCartItemId) {

        CartEntity cartEntity = cartRepository.findByUserId(userId);
        log.info("Cart details {}",cartEntity);

        if (isNull(cartEntity)) {
            throw new CartItemNotFoundException(Constants.CART_NOT_FOUND + userId);
        }

        if(cartEntity.getCustomizedCartItemList().removeIf(item -> item.getCustomizedCartItemId().equals(customizedCartItemId))){
            cartRepository.save(cartEntity);
            log.info("Customized cart item deleted successfully: {}", cartEntity);
        }
        else
            throw new CartItemNotFoundException(Constants.CART_ITEM_NOT_FOUND + customizedCartItemId);

        return new SuccessResponse(Constants.CART_ITEM_DELETED, HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse updateCustomizedCartItem(UpdateCartItemRequest cartRequest) {

        CartEntity cartEntity = cartRepository.findByUserId(cartRequest.getUserId());

        if (isNull(cartEntity)) {
            throw new CartItemNotFoundException(Constants.CART_NOT_FOUND + cartRequest.getUserId());
        }
        List<CustomizedCartItemEntity> customizedCartItems = cartEntity.getCustomizedCartItemList();

        for (CustomizedCartItemEntity customizedItem : customizedCartItems) {
            if (customizedItem.getCustomizedCartItemId().equals(cartRequest.getCustomizedCartItemId())) {
                customizedItem.setQuantity(cartRequest.getQuantity());
                break;
            }
        }

        cartRepository.save(cartEntity);

        return new SuccessResponse(Constants.CART_ITEM_UPDATE, HttpStatus.OK.value());
    }

    private CustomizedAddDataResponse mapToCustomizedAddDataResponse(CustomizedAddDataRequest request) {
        CustomizedAddDataResponse response = new CustomizedAddDataResponse();
        response.setPantType(request.getPantType());
        response.setTrueWaistMeasurement(request.getTrueWaistMeasurement());
        response.setPantInSeamLength(request.getPantInSeamLength());
        response.setPantOutSeamLength(request.getPantOutSeamLength());
        response.setFitType(request.getFitType());
        response.setRiseType(request.getRiseType());
        response.setFrontPocketType(request.getFrontPocketType());
        response.setBackPocketType(request.getBackPocketType());
        response.setFrontButtonType(request.getFrontButtonType());
        response.setBackButtonType(request.getBackButtonType());
        response.setPantPleatType(request.getPantPleatType());
        response.setFlyType(request.getFlyType());
        response.setPantCuffsType(request.getPantCuffsType());
        response.setFabric(request.getFabricDetails());
        return response;
    }

    private CustomizedCartItemEntity getCustomizedCartItemEntity(CustomizedCartItemRequest customizedCartItemRequest, CustomizedAddDataResponse customizedAddDataResponse) {
        CustomizedCartItemEntity customizedCartItemEntity = new CustomizedCartItemEntity();
        customizedCartItemEntity.setCustomizedCartItemId(generator.generateId(Constants.CART_ITEM_ID));
        customizedCartItemEntity.setQuantity(customizedCartItemRequest.getQuantity());

        JsonNode fabricDetailsNode = customizedAddDataResponse.getFabric();
        if (!isNull(fabricDetailsNode)) {
            try {
                Fabric fabricDetails = objectMapper.treeToValue(fabricDetailsNode, Fabric.class);
                customizedCartItemEntity.setColor(fabricDetails.getFabricColor());
                customizedCartItemEntity.setColorCode(fabricDetails.getFabricColorCode());
                customizedCartItemEntity.setPrice(fabricDetails.getFabricPrice());
                customizedCartItemEntity.setProductOfferPercentage(fabricDetails.getProductOfferPercentage());
                Optional<String> firstImageUrl = fabricDetails.getImageUrl()
                        .stream()
                        .findFirst();

                firstImageUrl.ifPresent(customizedCartItemEntity::setProductImageUrl);
            } catch (ConvertFromJsonException | JsonProcessingException e) {
                throw new FabricNotFoundException(Constants.FABRIC_PARSE_ERROR);
            }
        }
        Map<String, Object> productDetails = objectMapper.convertValue(customizedAddDataResponse, new TypeReference<>() {});

        customizedCartItemEntity.setCustomizedProductDetails(productDetails);

        return customizedCartItemEntity;
    }

    public boolean isSameProduct(CustomizedCartItemEntity existingCustomizedCartItem, CustomizedAddDataResponse newCustomizedData) {

        Map<String, Object> existingProductDetails = existingCustomizedCartItem.getCustomizedProductDetails();
        Map<String, Object> newProductDetails = objectMapper.convertValue(newCustomizedData, new TypeReference<>() {});

        return Objects.equals(existingProductDetails, newProductDetails);
    }
}