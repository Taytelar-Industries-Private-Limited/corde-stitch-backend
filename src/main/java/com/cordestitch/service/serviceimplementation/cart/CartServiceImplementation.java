package com.cordestitch.service.serviceimplementation.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.cart.CartEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.entity.cart.CustomizedCartItemEntity;
import com.cordestitch.entity.product.ColorQuantity;
import com.cordestitch.entity.product.Product;
import com.cordestitch.entity.product.ProductImage;
import com.cordestitch.entity.product.StockQuantity;
import com.cordestitch.exception.cart.CartItemNotFoundException;
import com.cordestitch.repository.cart.CartRepository;
import com.cordestitch.repository.product.ProductRepository;
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.request.cart.CartRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartItemResponse;
import com.cordestitch.response.cart.CartResponse;
import com.cordestitch.response.cart.GetCartItemProductDetails;
import com.cordestitch.response.customization.CustomizationCartResponse;
import com.cordestitch.response.customization.CustomizedAddDataResponse;
import com.cordestitch.response.product.ProductDataResponse;
import com.cordestitch.response.product.ProductImageResponse;
import com.cordestitch.response.product.ProductResponse;
import com.cordestitch.service.service.cart.CartService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImplementation implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final Generator generator;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private static final String PRODUCT_CACHE_NAME = "productsCache";
    private static final String PRODUCT_CACHE_KEY = "listAllProduct";

    @Transactional
    @Override
    public SuccessResponse addToCart(CartRequest cartRequest) {
        log.info("Add to Cart Request : {}", cartRequest);
        CartEntity cartEntity = cartRepository.findByUserId(cartRequest.getUserId());

        if (isNull(cartEntity)) {
            CartEntity cart = new CartEntity();
            cart.setCartId(generator.generateId(Constants.CART_ID));
            cart.setUserId(cartRequest.getUserId());
            cart.setCartItemEntityList(getCartItemEntityList(cartRequest.getCartItemRequests()));
            cartRepository.save(cart);
        } else {
            List<CartItemEntity> existingCartItems = cartEntity.getCartItemEntityList();
            for (CartItemRequest requestItem : cartRequest.getCartItemRequests()) {
                boolean itemExists = false;

                for (CartItemEntity existingItem : existingCartItems) {
                    if (existingItem.getProductId().equals(requestItem.getProductId()) &&
                            existingItem.getProductSize().equals(requestItem.getProductSize()) &&
                            existingItem.getProductColor().equals(requestItem.getProductColor())) {
                        existingItem.setQuantity(existingItem.getQuantity() + requestItem.getQuantity());
                        existingItem.setPrice(requestItem.getPrice() * existingItem.getQuantity());
                        itemExists = true;
                        break;
                    }
                }
                if (!itemExists) {
                    existingCartItems.add(convertToCartItemEntity(requestItem));
                }
            }
            cartEntity.setCartItemEntityList(existingCartItems);
            cartRepository.save(cartEntity);
        }

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage(Constants.ADDED_TO_CART_SUCCESSFULLY);
        successResponse.setStatusCode(HttpStatus.OK.value());

        log.info("Add to Cart Response: " + successResponse);
        return successResponse;
    }

    @Transactional
    @Override
    public SuccessResponse updateCartItem(CartRequest cartRequest) {
        log.info("Update Cart Item request : {}", cartRequest);
        CartEntity cartEntity = cartRepository.findByUserId(cartRequest.getUserId());

        if (!isNull(cartEntity)) {
            List<CartItemEntity> cartItems = cartEntity.getCartItemEntityList();
            for (CartItemRequest cartItemRequest : cartRequest.getCartItemRequests()) {
                String cartItemId = cartItemRequest.getCartItemId();

                updateCartItemRequest(cartItemId, cartItemRequest, cartItems);
            }
            cartRepository.save(cartEntity);

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage(Constants.CART_ITEM_UPDATE);
            successResponse.setStatusCode(HttpStatus.OK.value());

            log.info("Update cart item response : {}", successResponse);
            return successResponse;
        } else {
            log.error("CartServiceImplementation, updateCart, Cart not found for user: {}", cartRequest.getUserId());
            throw new CartItemNotFoundException("Cart not found for user: " + cartRequest.getUserId());
        }
    }

    @Override
    public SuccessResponse deleteCartItem(CartRequest cartRequest) {
        log.info("Delete Cart Item request : {}", cartRequest);
        CartEntity cartEntity = cartRepository.findByUserId(cartRequest.getUserId());

        if (!isNull(cartEntity)) {
            List<CartItemEntity> cartItems = cartEntity.getCartItemEntityList();
            for (CartItemRequest cartItemRequest : cartRequest.getCartItemRequests()) {
                String cartItemId = cartItemRequest.getCartItemId();
                cartItems.removeIf(cartItem -> cartItem.getCartItemId().equals(cartItemId));
            }
            cartEntity.setCartItemEntityList(cartItems);
            cartRepository.save(cartEntity);

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage(Constants.CART_ITEM_DELETED);
            successResponse.setStatusCode(HttpStatus.OK.value());

            log.info("Delete cart item response : {}", successResponse);
            return successResponse;

        } else {
            log.error("CartServiceImplementation, updateCart," + Constants.CART_NOT_FOUND + ": {}", cartRequest.getUserId());
            throw new CartItemNotFoundException(Constants.CART_NOT_FOUND + ": " + cartRequest.getUserId());
        }
    }

    @Override
    public CartResponse getCartItems(String userId) {
        log.info("Get cart items request for the userId : {}", userId);
        CartEntity cartEntity = cartRepository.findByUserId(userId);
        CartResponse cartResponse = new CartResponse();

        if (!isNull(cartEntity)) {
            cartResponse.setCartId(cartEntity.getCartId());
            cartResponse.setCartItemResponses(cartEntity.getCartItemEntityList() != null && !cartEntity.getCartItemEntityList().isEmpty() ? getCartItemResponse(cartEntity.getCartItemEntityList()) : new ArrayList<>());
            cartResponse.setCustomizationCartResponses(cartEntity.getCustomizedCartItemList() != null && !cartEntity.getCustomizedCartItemList().isEmpty() ? getCustomizationCartResponse(cartEntity.getCustomizedCartItemList()) : new ArrayList<>());
        } else {
            cartResponse.setCartItemResponses(new ArrayList<>());
            cartResponse.setCustomizationCartResponses(new ArrayList<>());
        }
        log.info("Get cart items response : {}", cartResponse);
        return cartResponse;
    }

    private List<CartItemResponse> getCartItemResponse(List<CartItemEntity> cartItemEntityList) {
        return cartItemEntityList.stream()
                .map(cartItemEntity -> {
                    CartItemResponse cartItemResponse = new CartItemResponse();
                    cartItemResponse.setCartItemId(cartItemEntity.getCartItemId());
                    cartItemResponse.setProductId(cartItemEntity.getProductId());
                    cartItemResponse.setProductName(cartItemEntity.getProductName());
                    cartItemResponse.setProductSize(cartItemEntity.getProductSize());
                    cartItemResponse.setProductColor(cartItemEntity.getProductColor());
                    cartItemResponse.setProductColorCode(cartItemEntity.getProductColorCode());
                    cartItemResponse.setQuantity(cartItemEntity.getQuantity());
                    cartItemResponse.setPrice(cartItemEntity.getPrice());
                    cartItemResponse.setProductOfferPercentage(cartItemEntity.getProductOfferPercentage());
                    cartItemResponse.setProductImagesUrl(cartItemEntity.getProductImageUrl());
                    GetCartItemProductDetails productDetails = getProductDetails(
                            cartItemEntity.getProductId(),
                            cartItemEntity.getProductSize(),
                            cartItemEntity.getProductColor()
                    );
                    cartItemResponse.setProductStocksAvailable(productDetails.getStocksAvailable());
                    cartItemResponse.setProductAvailableSizes(productDetails.getAvailableSizes());
                    cartItemResponse.setProductAvailableColors(productDetails.getAvailableColors());
                    return cartItemResponse;
                })
                .toList();
    }

    public GetCartItemProductDetails getProductDetails(String productId, Integer productSize, String productColor) {
        Optional<Product> productOptional = productRepository.findByProductId(productId);
        Integer stocksAvailable = 0;
        List<Integer> availableSizes = new ArrayList<>();
        Map<String, String> availableColors = new HashMap<>();

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            for (StockQuantity stockQuantity : product.getStockQuantities()) {
                availableSizes.add(stockQuantity.getSize());
                for (ColorQuantity colorQuantity : stockQuantity.getColorQuantities()) {
                    availableColors.put(colorQuantity.getColor(), colorQuantity.getColorCode());
                }
                if (stockQuantity.getSize().equals(productSize)) {
                    Optional<ColorQuantity> matchingColorQuantity = stockQuantity.getColorQuantities().stream()
                            .filter(colorQuantity -> colorQuantity.getColor().equalsIgnoreCase(productColor))
                            .findFirst();

                    if (matchingColorQuantity.isPresent()) {
                        stocksAvailable = matchingColorQuantity.get().getQuantity();
                    }
                }
            }
        }
        return new GetCartItemProductDetails(stocksAvailable, availableSizes, availableColors);
    }

    private List<CartItemEntity> getCartItemEntityList(List<CartItemRequest> cartItemRequests) {


        return cartItemRequests.stream()
                .map(cartItemRequest -> {
                    CartItemEntity itemEntity = new CartItemEntity();
                    itemEntity.setCartItemId(generator.generateId(Constants.CART_ITEM_ID));
                    itemEntity.setProductId(cartItemRequest.getProductId());
                    itemEntity.setProductName(cartItemRequest.getProductName());
                    itemEntity.setProductSize(cartItemRequest.getProductSize());
                    itemEntity.setProductColor(cartItemRequest.getProductColor());
                    itemEntity.setProductColorCode(cartItemRequest.getProductColorCode());
                    itemEntity.setQuantity(cartItemRequest.getQuantity());
                    itemEntity.setPrice(cartItemRequest.getPrice());
                    itemEntity.setProductOfferPercentage(cartItemRequest.getProductOfferPercentage());
                    itemEntity.setProductImageUrl(mapToProductImageUrl(cartItemRequest.getProductId(), cartItemRequest.getProductColor()));

                    return itemEntity;
                })
                .toList();
    }

    private List<CustomizationCartResponse> getCustomizationCartResponse(List<CustomizedCartItemEntity> customizedCartItemList) {
        return customizedCartItemList.stream()
                .map(this::mapToCustomizationCartResponse)
                .toList();
    }

    private CustomizationCartResponse mapToCustomizationCartResponse(CustomizedCartItemEntity customizedCartItemEntity) {
        CustomizationCartResponse customizationCartResponse = new CustomizationCartResponse();
        customizationCartResponse.setCustomizedCartItemId(customizedCartItemEntity.getCustomizedCartItemId());
        customizationCartResponse.setQuantity(customizedCartItemEntity.getQuantity());
        customizationCartResponse.setPrice(customizedCartItemEntity.getPrice());
        customizationCartResponse.setColor(customizedCartItemEntity.getColor());
        customizationCartResponse.setColorCode(customizedCartItemEntity.getColorCode());
        customizationCartResponse.setProductOfferPercentage(customizedCartItemEntity.getProductOfferPercentage());
        customizationCartResponse.setProductImageUrl(customizedCartItemEntity.getProductImageUrl());

        customizationCartResponse.setCustomizedAddDataResponse(objectMapper.convertValue(customizedCartItemEntity.getCustomizedProductDetails(), CustomizedAddDataResponse.class));

        return customizationCartResponse;
    }

    private CartItemEntity convertToCartItemEntity(CartItemRequest cartItemRequest) {
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCartItemId(generator.generateId(Constants.CART_ITEM_ID));
        cartItemEntity.setProductId(cartItemRequest.getProductId());
        cartItemEntity.setProductName(cartItemRequest.getProductName());
        cartItemEntity.setProductSize(cartItemRequest.getProductSize());
        cartItemEntity.setProductColor(cartItemRequest.getProductColor());
        cartItemEntity.setProductColorCode(cartItemRequest.getProductColorCode());
        cartItemEntity.setQuantity(cartItemRequest.getQuantity());
        cartItemEntity.setPrice(cartItemRequest.getPrice());
        cartItemEntity.setProductOfferPercentage(cartItemRequest.getProductOfferPercentage());
        if (isNull(cartItemRequest.getProductImageUrl())) {
            cartItemEntity.setProductImageUrl(mapToProductImageUrl(cartItemRequest.getProductId(), cartItemRequest.getProductColor()));
        }
        cartItemEntity.setProductImageUrl(cartItemRequest.getProductImageUrl());

        return cartItemEntity;
    }

    public void updateCartItemRequest(String cartItemId, CartItemRequest cartItemRequest, List<CartItemEntity> cartItems) {
        for (CartItemEntity cartItem : cartItems) {
            if (cartItem.getCartItemId().equals(cartItemId)) {
                boolean colorChanged = !cartItem.getProductColor().equals(cartItemRequest.getProductColor());

                cartItem.setProductId(cartItemRequest.getProductId());
                cartItem.setProductName(cartItemRequest.getProductName());
                cartItem.setProductSize(cartItemRequest.getProductSize());
                cartItem.setProductColor(cartItemRequest.getProductColor());
                cartItem.setProductColorCode(cartItemRequest.getProductColorCode());
                cartItem.setQuantity(cartItemRequest.getQuantity());
                cartItem.setPrice(cartItemRequest.getPrice());
                cartItem.setProductOfferPercentage(cartItemRequest.getProductOfferPercentage());
                if (colorChanged) {
                    cartItem.setProductImageUrl(mapToProductImageUrl(cartItemRequest.getProductId(), cartItemRequest.getProductColor()));
                }
                break;
            }
        }
    }

    public String mapToProductImageUrl(String productId, String productColor) {
        Cache cache = cacheManager.getCache(PRODUCT_CACHE_NAME);
        if (cache != null) {
            ProductResponse productResponse = cache.get(PRODUCT_CACHE_KEY, ProductResponse.class);
            if (productResponse != null) {
                return fromCacheImageURLFetching(productResponse, productId, productColor);
            }
        }
        log.info("Cache miss for productId: {} with color : {} at {}", productId, productColor, LocalDateTime.now(ZoneId.of(Constants.ZONE)));

        String firstImageUrl = fromDBImageURLFetching(productId, productColor);
        log.info("Getting the Image URL for productId: {} from the DB at {}", productId, LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        return firstImageUrl;
    }

    private String fromCacheImageURLFetching(ProductResponse productResponse, String productId, String productColor) {
        Optional<ProductDataResponse> response = productResponse.getCategoryResponses().stream()
                .flatMap(categoryResponse -> categoryResponse.getSubCategoryResponses().stream())
                .flatMap(subCategoryResponse -> subCategoryResponse.getProductDataResponses().stream())
                .filter(response1 -> response1.getProductId().equals(productId))
                .findFirst();

        if (response.isPresent()) {
            ProductDataResponse productDataResponse = response.get();

            String firstImageUrl = productDataResponse.getProductImageResponses().stream()
                    .filter(productImageResponse -> productImageResponse.getColorName().equalsIgnoreCase(productColor))
                    .sorted(Comparator.comparingInt(ProductImageResponse::getImagePriority))
                    .map(ProductImageResponse::getImageUrl)
                    .findFirst()
                    .orElse(null);

            if (firstImageUrl != null) {
                log.info("Getting the Image URL for productId: {} from the Cache at {}", productId, LocalDateTime.now(ZoneId.of(Constants.ZONE)));
                return firstImageUrl;
            }
        }
        return null;
    }

    private String fromDBImageURLFetching(String productId, String productColor) {
        Optional<Product> productOptional = productRepository.findByProductId(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            return product.getProductImages().stream()
                    .filter(productImage -> productImage.getColorName().equalsIgnoreCase(productColor))
                    .sorted(Comparator.comparingInt(ProductImage::getImagePriority))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

}
