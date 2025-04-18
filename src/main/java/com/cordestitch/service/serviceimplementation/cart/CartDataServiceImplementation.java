package com.cordestitch.service.serviceimplementation.cart;

import com.cordestitch.entity.cart.CartDataEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.exception.cart.SessionExpiredException;
import com.cordestitch.exception.token.JwtProcessingException;
import com.cordestitch.repository.cart.CartDataRepository;
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartItemResponse;
import com.cordestitch.response.cart.GetCartItemProductDetails;
import com.cordestitch.service.service.cart.CartDataService;
import com.cordestitch.service.serviceimplementation.token.CookieService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.*;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartDataServiceImplementation implements CartDataService {

    private final CookieService cookieService;

    private final Generator generator;

    private final CartDataRepository cartDataRepository;

    private final CartServiceImplementation cartServiceImplementation;

    @Value("${spring.app.env}")
    private String environment;

    @Value("${application.security.token.secret-key}")
    private String secretKey;

    @Value("${application.security.token.expiration}")
    private long tokenExpiration;

    private static final String TYPE = "C";
    private static final String TOKEN_ID = "tokenId";
    private static final String ID = "id";
    private static final String IP = "ip";

    @Override
    public SuccessResponse generateSession(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = generator.generateId(Constants.SESSION_ID) + System.currentTimeMillis();
        String deviceId = generator.generateId(Constants.DEVICE_ID);

        String token = generateToken(request, sessionId, deviceId);
        cookieService.createCookie(token, sessionId, deviceId, response);
        return new SuccessResponse(Constants.SUCCESS, HttpStatus.OK.value());
    }

    @Transactional
    @Override
    public SuccessResponse addProductToCart(CartItemRequest cartItemRequest, HttpServletRequest request) {
        log.info("Add Product to Cart request: {}", cartItemRequest);
        Map<String, String> cookieValues = extractCookieValues(request);
        String tokenId = cookieValues.get(TOKEN_ID);
        String sessionId = cookieValues.get(ID);
        String deviceId = cookieValues.get(IP);

        checkSessionValid(tokenId, sessionId, deviceId, request);

        CartDataEntity cartDataEntity = cartDataRepository.findByDeviceIdAndTokenId(deviceId, tokenId);
        if (isNull(cartDataEntity)) {
            CartDataEntity cartData = new CartDataEntity();
            cartData.setSessionId(sessionId);
            cartData.setTokenId(tokenId);
            cartData.setDeviceId(deviceId);
            cartData.setTokenExpiry(System.currentTimeMillis() + tokenExpiration);
            cartData.setCartDataItemEntities(mapToCartDataItemEntity(cartItemRequest));
            cartDataRepository.save(cartData);
        } else {
            validateExpirationTime(cartDataEntity);
            List<CartItemEntity> existingCartItems = new ArrayList<>(cartDataEntity.getCartDataItemEntities());
            boolean itemExists = false;

            for (CartItemEntity existingItem : existingCartItems) {
                if (existingItem.getProductId().equals(cartItemRequest.getProductId()) &&
                        existingItem.getProductSize().equals(cartItemRequest.getProductSize()) &&
                        existingItem.getProductColor().equals(cartItemRequest.getProductColor())) {
                    existingItem.setQuantity(existingItem.getQuantity() + cartItemRequest.getQuantity());
                    existingItem.setPrice(cartItemRequest.getPrice() * existingItem.getQuantity());
                    itemExists = true;
                    break;
                }
            }
            if (!itemExists) {
                existingCartItems.add(convertToCartItemEntity(cartItemRequest));
            }

            cartDataEntity.setCartDataItemEntities(existingCartItems);
            cartDataRepository.save(cartDataEntity);
        }

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage(Constants.ADDED_TO_CART_SUCCESSFULLY);
        successResponse.setStatusCode(HttpStatus.OK.value());

        log.info("Add to Cart Response: " + successResponse);
        return successResponse;
    }

    @Override
    public List<CartItemResponse> fetchCartData(HttpServletRequest request) {
        Map<String, String> cookieValues = extractCookieValues(request);
        String tokenId = cookieValues.get(TOKEN_ID);
        String sessionId = cookieValues.get(ID);
        String deviceId = cookieValues.get(IP);

        checkSessionValid(tokenId, sessionId, deviceId, request);

        CartDataEntity cartDataEntity = cartDataRepository.findByDeviceIdAndTokenId(deviceId, tokenId);

        if (isNull(cartDataEntity)) {
            return new ArrayList<>();
        } else {
            validateExpirationTime(cartDataEntity);
            return cartDataEntity.getCartDataItemEntities().stream()
                    .map(this::maptoCartItemResponse)
                    .toList();
        }
    }

    @Transactional
    @Override
    public SuccessResponse updateCartItem(CartItemRequest cartItemRequest, HttpServletRequest request) {
        log.info("Update cart item request: {}", cartItemRequest);
        Map<String, String> cookieValues = extractCookieValues(request);
        String tokenId = cookieValues.get(TOKEN_ID);
        String sessionId = cookieValues.get(ID);
        String deviceId = cookieValues.get(IP);

        checkSessionValid(tokenId, sessionId, deviceId, request);

        CartDataEntity cartDataEntity = cartDataRepository.findByDeviceIdAndTokenId(deviceId, tokenId);
        if (!isNull(cartDataEntity)) {
            validateExpirationTime(cartDataEntity);
            List<CartItemEntity> cartItems = cartDataEntity.getCartDataItemEntities();
            String cartItemId = cartItemRequest.getCartItemId();
            cartServiceImplementation.updateCartItemRequest(cartItemId, cartItemRequest, cartItems);
            cartDataRepository.save(cartDataEntity);

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage(Constants.CART_ITEM_UPDATE);
            successResponse.setStatusCode(HttpStatus.OK.value());

            log.info("Update cart item response : {}", successResponse);
            return successResponse;
        } else {
            log.error("While updating, No cart data found for session id: {}, token id: {}", sessionId, tokenId);
            return new SuccessResponse(Constants.CART_NOT_FOUND, HttpStatus.NOT_FOUND.value());
        }
    }

    @Override
    public SuccessResponse removeProductFromCart(CartItemRequest cartItemRequest, HttpServletRequest request) {
        log.info("Remove Product from Cart request: {}", cartItemRequest);
        Map<String, String> cookieValues = extractCookieValues(request);
        String tokenId = cookieValues.get(TOKEN_ID);
        String sessionId = cookieValues.get(ID);
        String deviceId = cookieValues.get(IP);

        checkSessionValid(tokenId, sessionId, deviceId, request);

        CartDataEntity cartDataEntity = cartDataRepository.findByDeviceIdAndTokenId(deviceId, tokenId);
        if (!isNull(cartDataEntity)) {
            validateExpirationTime(cartDataEntity);
            List<CartItemEntity> cartItems = new ArrayList<>(cartDataEntity.getCartDataItemEntities());
            String cartItemId = cartItemRequest.getCartItemId();

            cartItems.removeIf(cartItem -> cartItem.getCartItemId().equals(cartItemId));
            cartDataEntity.setCartDataItemEntities(cartItems);
            cartDataRepository.save(cartDataEntity);

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage(Constants.CART_ITEM_DELETED);
            successResponse.setStatusCode(HttpStatus.OK.value());

            log.info("Remove from Cart response : {}", successResponse);
            return successResponse;
        } else {
            log.error("while removing, No cart data found for session id: {}, token id: {}", sessionId, tokenId);
            return new SuccessResponse(Constants.CART_NOT_FOUND, HttpStatus.NOT_FOUND.value());
        }
    }

    @Override
    public SuccessResponse deleteCartItems(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> cookieValues = extractCookieValues(request);
        String tokenId = cookieValues.get(TOKEN_ID);
        String sessionId = cookieValues.get(ID);
        String deviceId = cookieValues.get(IP);

        checkSessionValid(tokenId, sessionId, deviceId, request);
        CartDataEntity cartDataEntity = cartDataRepository.findByDeviceIdAndTokenId(deviceId, tokenId);
        if(!isNull(cartDataEntity)) {
            validateExpirationTime(cartDataEntity);
            cartDataRepository.delete(cartDataEntity);
            cookieService.clearCookies(response);

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage(Constants.CART_ITEM_DELETED);
            successResponse.setStatusCode(HttpStatus.OK.value());

            log.info("Deleted Cart Items from CartDate Entity : {}", successResponse);
            return successResponse;
        } else {
            log.error("While deleting, No cart data found for session id: {}, token id: {}", sessionId, tokenId);
            return new SuccessResponse(Constants.CART_NOT_FOUND, HttpStatus.OK.value());
        }
    }

    @Scheduled(fixedRate = 1800000)
    public SuccessResponse cleanUpExpirationData() {
        log.info("Running scheduled task to clean up expired cart data.");
        SuccessResponse successResponse;

        Long currentTime = System.currentTimeMillis();
        List<CartDataEntity> expiredCartData = cartDataRepository.findByTokenExpiryLessThan(currentTime);
        log.info("List Of Cart Data: {}", expiredCartData);

        if (!expiredCartData.isEmpty()) {
            log.info("Found {} expired cart data entries. Deleting...", expiredCartData.size());
            cartDataRepository.deleteAll(expiredCartData);
            successResponse = new SuccessResponse(Constants.EXPIRED_CART_DATA, HttpStatus.OK.value());
        } else {
            log.info("No expired cart data found.");
            successResponse = new SuccessResponse(Constants.NO_EXPIRED_CART_DATA, HttpStatus.OK.value());
        }

        log.info("Clean up expiration data response: {}", successResponse);
        return successResponse;
    }

    private CartItemResponse maptoCartItemResponse(CartItemEntity cartItemEntity) {
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
        GetCartItemProductDetails productDetails = cartServiceImplementation.getProductDetails(
                cartItemEntity.getProductId(),
                cartItemEntity.getProductSize(),
                cartItemEntity.getProductColor()
        );
        cartItemResponse.setProductStocksAvailable(productDetails.getStocksAvailable());
        cartItemResponse.setProductAvailableSizes(productDetails.getAvailableSizes());
        cartItemResponse.setProductAvailableColors(productDetails.getAvailableColors());

        return cartItemResponse;
    }

    private List<CartItemEntity> mapToCartDataItemEntity(CartItemRequest cartItemRequest) {
        List<CartItemEntity> cartItemEntities = new ArrayList<>();
        CartItemEntity itemEntity = convertToCartItemEntity(cartItemRequest);
        cartItemEntities.add(itemEntity);

        return cartItemEntities;
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
        cartItemEntity.setPrice(cartItemRequest.getPrice() * cartItemRequest.getQuantity());
        cartItemEntity.setProductOfferPercentage(cartItemRequest.getProductOfferPercentage());
        cartItemEntity.setProductImageUrl(cartServiceImplementation.mapToProductImageUrl(cartItemRequest.getProductId(), cartItemRequest.getProductColor()));

        return cartItemEntity;
    }

    public void checkSessionValid(String tokenId, String sessionId, String deviceId, HttpServletRequest request) {
        boolean isValid = validSession(tokenId, sessionId, deviceId, request);
        if (!isValid) {
            throw new SessionExpiredException(Constants.SESSION_HAS_EXPIRED);
        }
    }

    private boolean validSession(String tokenId, String sessionId, String deviceId, HttpServletRequest request) {
        try {
            Claims claims = parseClaims(tokenId);

            if (!claims.get(ID).equals(sessionId))
                return false;

            if (!claims.get(IP).equals(deviceId))
                return false;

            if (!claims.get("user").equals(request.getHeader(Constants.USER_AGENT)))
                return false;

            return !claims.getExpiration().before(new Date());

        } catch (ExpiredJwtException e) {
            log.error("Session has expired: {}", e.getMessage());
            throw new SessionExpiredException(Constants.SESSION_HAS_EXPIRED);
        } catch (JwtException e) {
            log.error("Error parsing session token: {}", e.getMessage());
            throw new JwtProcessingException("Invalid Session token");
        }
    }

    public Claims parseClaims(String tokenId) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseSignedClaims(tokenId)
                .getPayload();
    }

    public String generateToken(HttpServletRequest request, String sessionId, String deviceId) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(ID, sessionId);
        attributes.put("env", environment);
        attributes.put("type", TYPE);
        attributes.put(IP, deviceId);
        attributes.put("user", request.getHeader(Constants.USER_AGENT));
        attributes.put("timestamp", System.currentTimeMillis());
        attributes.put("host", request.getHeader(Constants.HOST));

        return buildToken(attributes);
    }

    private String buildToken(Map<String, Object> attributes) {
        return Jwts.builder()
                .claims(attributes)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Map<String, String> extractCookieValues(HttpServletRequest request) {
        Map<String, String> cookieValues = new HashMap<>();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_ID.equals(cookie.getName()) || ID.equals(cookie.getName()) || IP.equals(cookie.getName())) {
                    cookieValues.put(cookie.getName(), cookie.getValue());
                }
            }
        }
        return cookieValues;
    }

    private void validateExpirationTime(CartDataEntity cartDataEntity) {
        if(cartDataEntity.getTokenExpiry() < System.currentTimeMillis()) {
            throw new SessionExpiredException(Constants.SESSION_HAS_EXPIRED);
        }
    }
}