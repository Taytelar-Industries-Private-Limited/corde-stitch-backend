package com.cordestitch.serviceimplementation.cart;

import com.cordestitch.entity.cart.CartDataEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.exception.cart.SessionExpiredException;
import com.cordestitch.exception.token.JwtProcessingException;
import com.cordestitch.repository.cart.CartDataRepository;
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.cart.CartItemResponse;
import com.cordestitch.service.serviceimplementation.cart.CartDataServiceImplementation;
import com.cordestitch.service.serviceimplementation.cart.CartServiceImplementation;
import com.cordestitch.service.serviceimplementation.token.CookieService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CartDataServiceImplementationTest {

    @InjectMocks
    public CartDataServiceImplementation cartDataServiceImplementation;

    @Mock
    public CartDataRepository cartDataRepository;

    @Mock
    public CartServiceImplementation cartServiceImplementation;

    @Mock
    public CookieService cookieService;

    @Mock
    public Generator generator;

    @Mock
    public HttpServletRequest httpServletRequest;

    @Mock
    public HttpServletResponse httpServletResponse;

    @BeforeEach
    void setup() throws NoSuchAlgorithmException {
        MockitoAnnotations.openMocks(this);
        String secureKey = getSecureKey();

        ReflectionTestUtils.setField(cartDataServiceImplementation, "environment", "test");
        ReflectionTestUtils.setField(cartDataServiceImplementation, "secretKey", secureKey);
        ReflectionTestUtils.setField(cartDataServiceImplementation, "tokenExpiration", 3600000);

    }

    @Test
    void testGenerateSession_Success() {
        SuccessResponse successResponse = cartDataServiceImplementation.generateSession(httpServletRequest, httpServletResponse);
        assertEquals(Constants.SUCCESS, successResponse.getMessage());
    }

    @Test
    void testAddProductToCart_Success() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(null);
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest);
        assertEquals(Constants.ADDED_TO_CART_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testAddProductToCart_When_TokenExpiry_TimesHasBeenExpired() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));

        CartDataEntity cartDataEntity = getCartDataEntity();
        cartDataEntity.setTokenExpiry(System.currentTimeMillis() - 1000);

        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(cartDataEntity);
        mockValidSession(tokenId);
        assertThrows(SessionExpiredException.class, () -> cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest));
    }

    @Test
    void testAddProductToCart_SameProduct_When_CartDataEntity_Already_Present_ForThe_DeviceID() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(getCartDataEntity());
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest);
        assertEquals(Constants.ADDED_TO_CART_SUCCESSFULLY, successResponse.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "'12345', 1, 'red'",
            "'78901', 2, 'green'",
            "'78901', 40, 'red'"
    })
    void testAddProductToCart_Parameterized(String productId, int productSize, String productColor) {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setProductSize(productSize);
        cartItemRequest.setProductColor(productColor);

        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(getCartDataEntity1());
        mockValidSession(tokenId);

        SuccessResponse successResponse = cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest);
        assertEquals(Constants.ADDED_TO_CART_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testFetchCartData_Has_Empty_Data() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(null);
        mockValidSession(tokenId);
        List<CartItemResponse> cartResponse = cartDataServiceImplementation.fetchCartData(httpServletRequest);
        assertEquals(0, cartResponse.size());
    }

    @Test
    void testUpdateCartItem_Success() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(getCartDataEntity());
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.updateCartItem(cartItemRequest, httpServletRequest);
        assertEquals(Constants.CART_ITEM_UPDATE, successResponse.getMessage());
    }

    @Test
    void testUpdateCartItem_When_NotFound() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        cartItemRequest.setCartItemId("invalid-cart-item-id");
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(null);
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.updateCartItem(cartItemRequest, httpServletRequest);
        assertEquals(Constants.CART_NOT_FOUND, successResponse.getMessage());
    }

    @Test
    void testRemoveCartItem_Success() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        cartItemRequest.setCartItemId("123456");
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(getCartDataEntity());
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.removeProductFromCart(cartItemRequest, httpServletRequest);
        assertEquals(Constants.CART_ITEM_DELETED, successResponse.getMessage());
    }

    @Test
    void testRemoveCartItem_When_NotFound() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(null);
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.removeProductFromCart(cartItemRequest, httpServletRequest);
        assertEquals(Constants.CART_NOT_FOUND, successResponse.getMessage());
    }

    @Test
    void testDeleteCartDataEntity_Success() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(getCartDataEntity());
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.deleteCartItems(httpServletRequest, httpServletResponse);
        assertEquals(Constants.CART_ITEM_DELETED, successResponse.getMessage());
    }

    @Test
    void testDeleteCartDataEntity_When_NotFound() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(null);
        mockValidSession(tokenId);
        SuccessResponse successResponse = cartDataServiceImplementation.deleteCartItems(httpServletRequest, httpServletResponse);
        assertEquals(Constants.CART_NOT_FOUND, successResponse.getMessage());
    }

    @Test
    void testCleanUpDataExpiration_Success() {
        List<CartDataEntity> cartDataEntities = List.of(getCartDataEntity());
        when(cartDataRepository.findByTokenExpiryLessThan(any())).thenReturn(cartDataEntities);
        SuccessResponse successResponse = cartDataServiceImplementation.cleanUpExpirationData();
        assertEquals(Constants.EXPIRED_CART_DATA, successResponse.getMessage());
    }

    @Test
    void testCleanUpDataExpiration_WhenNoData_PresentInEntity() {
        List<CartDataEntity> cartDataEntities = List.of();
        when(cartDataRepository.findByTokenExpiryLessThan(any())).thenReturn(cartDataEntities);
        SuccessResponse successResponse = cartDataServiceImplementation.cleanUpExpirationData();
        assertEquals(Constants.NO_EXPIRED_CART_DATA, successResponse.getMessage());
    }

    @Test
    void testAddProduct_SessionExpired_When_SessionIdIsDifferent() {
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        mockValidSession(tokenId);
        assertThrows(SessionExpiredException.class, () -> cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest));
    }

    @Test
    void testAddProduct_SessionExpired_When_DeviceId_IsDifferent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("user-agent");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent-different");
        String tokenId = cartDataServiceImplementation.generateToken(request, "session123", "device");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        mockValidSession(tokenId);
        assertThrows(SessionExpiredException.class, () -> cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest));
    }

    @Test
    void testAddProduct_SessionExpired_WhenTimeHasBeenExpired() {
        ReflectionTestUtils.setField(cartDataServiceImplementation, "tokenExpiration", 10);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = cartDataServiceImplementation.generateToken(httpServletRequest, "session123", "device123");

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        mockTimeExpiredSession(tokenId);
        assertThrows(SessionExpiredException.class, () -> cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest));
    }


    @Test
    void testAddProduct_SessionExpired_Throws_JwtProcessingException() {
        ReflectionTestUtils.setField(cartDataServiceImplementation, "tokenExpiration", 100);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("user-agent");
        String tokenId = "invalid.jwt.token";

        CartItemRequest cartItemRequest = getCartItemRequest();
        when(httpServletRequest.getCookies()).thenReturn(getCookies(tokenId));
        mockTimeExpiredSession(tokenId);
        assertThrows(JwtProcessingException.class, () -> cartDataServiceImplementation.addProductToCart(cartItemRequest, httpServletRequest));
    }

    private Cookie[] getCookies(String tokenId) {
        return new Cookie[]{
                new Cookie("tokenId", tokenId),
                new Cookie("id", "session123"),
                new Cookie("ip", "device123")
        };
    }

    private void mockValidSession(String tokenId) {
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            Claims claims = mock(Claims.class);
            when(claims.get("id")).thenReturn("session123");
            when(claims.get("ip")).thenReturn("device123");
            when(claims.get("user")).thenReturn("user-agent");
            when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000));

            JwtParser parser = mock(JwtParser.class);
            Jws<Claims> jws = mock(Jws.class);

            when(jws.getPayload()).thenReturn(claims);
            when(parser.parseSignedClaims(tokenId)).thenReturn(jws);

            JwtParserBuilder parserBuilder = mock(JwtParserBuilder.class);
            when(parserBuilder.setSigningKey(getSecureKey())).thenReturn(parserBuilder);
            when(parserBuilder.build()).thenReturn(parser);
            mockedJwts.when(Jwts::parser).thenReturn(parserBuilder);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void mockTimeExpiredSession(String tokenId) {
        try (MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {
            Claims claims = mock(Claims.class);
            when(claims.get("id")).thenReturn("session123");
            when(claims.get("ip")).thenReturn("device123");
            when(claims.get("user")).thenReturn("user-agent");
            when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() - 10000));

            JwtParser parser = mock(JwtParser.class);
            Jws<Claims> jws = mock(Jws.class);

            when(jws.getPayload()).thenReturn(claims);
            when(parser.parseSignedClaims(tokenId)).thenReturn(jws);

            JwtParserBuilder parserBuilder = mock(JwtParserBuilder.class);
            when(parserBuilder.setSigningKey(getSecureKey())).thenReturn(parserBuilder);
            when(parserBuilder.build()).thenReturn(parser);
            mockedJwts.when(Jwts::parser).thenReturn(parserBuilder);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private String getSecureKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    private CartItemRequest getCartItemRequest() {
        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setCartItemId("123456");
        cartItemRequest.setProductId("12345");
        cartItemRequest.setProductName("Test Product");
        cartItemRequest.setProductSize(1);
        cartItemRequest.setProductColor("Red");
        cartItemRequest.setProductColorCode("#FF0000");
        cartItemRequest.setQuantity(1);
        cartItemRequest.setPrice(100.0);
        cartItemRequest.setProductOfferPercentage(10.0);
        return cartItemRequest;
    }

    private CartDataEntity getCartDataEntity() {
        CartDataEntity cartDataEntity = new CartDataEntity();
        cartDataEntity.setDeviceId("device123");
        cartDataEntity.setTokenId("session123");
        cartDataEntity.setTokenExpiry(System.currentTimeMillis() + 3600000);
        cartDataEntity.setCartDataItemEntities(List.of(getCartItemEntity1(), getCartItemEntity2()));
        return cartDataEntity;
    }

    private CartDataEntity getCartDataEntity1() {
        CartDataEntity cartData = new CartDataEntity();
        cartData.setSessionId("session123");
        cartData.setDeviceId("device123");
        cartData.setTokenExpiry(System.currentTimeMillis() + 3600000);
        cartData.setCartDataItemEntities(List.of(getCartItemEntity2()));
        return cartData;
    }

    private CartItemEntity getCartItemEntity2() {
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCartItemId("789012");
        cartItemEntity.setProductId("78901");
        cartItemEntity.setProductName("Test Product 2");
        cartItemEntity.setProductSize(2);
        cartItemEntity.setProductColor("Blue");
        cartItemEntity.setProductColorCode("#0000FF");
        cartItemEntity.setQuantity(2);
        cartItemEntity.setPrice(200.0);
        cartItemEntity.setProductOfferPercentage(5.0);
        cartItemEntity.setProductImageUrl("http://images.com/2");
        return cartItemEntity;
    }

    private CartItemEntity getCartItemEntity1() {
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setCartItemId("123456");
        cartItemEntity.setProductId("12345");
        cartItemEntity.setProductName("Test Product");
        cartItemEntity.setProductSize(1);
        cartItemEntity.setProductColor("Red");
        cartItemEntity.setProductColorCode("#FF0000");
        cartItemEntity.setQuantity(1);
        cartItemEntity.setPrice(100.0);
        cartItemEntity.setProductOfferPercentage(10.0);
        cartItemEntity.setProductImageUrl("http://images.com");
        return cartItemEntity;
    }
}