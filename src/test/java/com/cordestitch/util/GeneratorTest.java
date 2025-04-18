package com.cordestitch.util;

import com.cordestitch.exception.otp.UnknownUserTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    private Generator generator;

    @BeforeEach
    void setUp() {
        generator = new Generator();
    }

    @Test
    void generateId_ShouldReturnExpectedId_WhenValidGeneratorIdProvided() {
        String orderId = generator.generateId(Constants.ORDER_ID);
        assertTrue(orderId.startsWith(Constants.ORDER_ID));
        assertEquals(10, orderId.length() - Constants.ORDER_ID.length());

        String orderItemId = generator.generateId(Constants.ORDER_ITEM_ID);
        assertTrue(orderItemId.startsWith(Constants.ORDER_ITEM_ID));
        assertEquals(8, orderItemId.length() - Constants.ORDER_ITEM_ID.length());

        String paymentId = generator.generateId(Constants.PAYMENT_ID);
        assertTrue(paymentId.startsWith(Constants.PAYMENT_ID));
        assertEquals(12, paymentId.length() - Constants.PRODUCT_ID.length());

        String userId = generator.generateId(Constants.USER_ID);
        assertTrue(userId.startsWith(Constants.USER_ID));
        assertEquals(6, userId.length() - Constants.USER_ID.length());

        String cardId = generator.generateId(Constants.CARD_ID);
        assertTrue(cardId.startsWith(Constants.CARD_ID));
        assertEquals(4, cardId.length() - Constants.CARD_ID.length());

        String nullId = generator.generateId(null);
        assertTrue(nullId.startsWith("null"));
    }

    @Test
    void referralCode_ShouldReturnValidReferralCode() {
        String referralCode = generator.referralCode();
        assertEquals(6, referralCode.length());
        assertTrue(referralCode.matches("[A-Za-z0-9-_]{6}"));
    }

    @Test
    void createAuthenticationSource_ShouldReturnValidAuthenticationSource() {
        String userType = generator.createAuthenticationSource(Constants.CUSTOMER);
        assertEquals(Constants.NORMAL_AUTHENTICATION, userType);

        String userTypeGoogle = generator.createAuthenticationSource(Constants.GOOGLE);
        assertEquals(Constants.GOOGLE_AUTHENTICATION, userTypeGoogle);

        String userTypeFacebook = generator.createAuthenticationSource(Constants.FACEBOOK);
        assertEquals(Constants.FACEBOOK_AUTHENTICATION, userTypeFacebook);

        String userTypeApple = generator.createAuthenticationSource(Constants.APPLE);
        assertEquals(Constants.APPLE_AUTHENTICATION, userTypeApple);

        String unknownUserType = "UNKNOWN_TYPE";
        UnknownUserTypeException exception = assertThrows(UnknownUserTypeException.class, () ->
                generator.createAuthenticationSource(unknownUserType));
        assertTrue(exception.getMessage().contains(Constants.UNKNOWN_USER_TYPE + unknownUserType));
    }

}