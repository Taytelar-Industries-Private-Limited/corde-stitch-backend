package com.cordestitch.util;

import com.cordestitch.exception.otp.UnknownUserTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@Slf4j
public class Generator {
    public String generateId(String generatorId) {
        UUID uuid = UUID.randomUUID();
        String numericUUID = new BigInteger(uuid.toString().replace("-", ""), 16).toString();
        String generatedId = switch (generatorId) {
            case Constants.DEVICE_ID -> numericUUID.substring(0, 32);
            case Constants.SESSION_ID -> numericUUID.substring(0, 16);
            case Constants.ORDER_ID -> numericUUID.substring(0, 10);
            case Constants.ORDER_ITEM_ID -> numericUUID.substring(0, 8);
            case Constants.PAYMENT_ID -> numericUUID.substring(0, 12);
            case Constants.USER_ID, Constants.CART_ID, Constants.CART_ITEM_ID, Constants.AFFILIATE_USER_ID,
                    Constants.ADMIN_ID, Constants.ADDRESS_ID, Constants.USER_CUSTOMIZATION_ID ->
                    numericUUID.substring(0, 6);
            case Constants.CATEGORY_ID, Constants.SUB_CATEGORY_ID, Constants.PRODUCT_ID,
                    Constants.STOCK_ID, Constants.COLOR_QUANTITY_ID,
                    Constants.CARD_ID,
                    Constants.SLOT_ID, Constants.HOME_PAGE_ID, Constants.REVIEW_ID,
                    Constants.RETURN_ID, Constants.LOYALTY_ID, Constants.LOYALTY_TRANSACTION_ID,
                    Constants.FAQS_ID, Constants.PRODUCT_IMAGE_ID, Constants.USER_BANK_ID, Constants.REFUND_ID ->
                    numericUUID.substring(0, 4);
            case null, default -> numericUUID;
        };

        return generatorId + generatedId;

    }

    public String referralCode() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidBytes = uuid.toString().getBytes(StandardCharsets.UTF_8);
        String base64EncodedUUID = Base64.getUrlEncoder().withoutPadding().encodeToString(uuidBytes);
        return base64EncodedUUID.substring(0, 6).toUpperCase();
    }

    public String createAuthenticationSource(String userType) {
        return switch (userType) {
            case Constants.CUSTOMER, Constants.AFFILIATE, Constants.ADMIN -> Constants.NORMAL_AUTHENTICATION;
            case Constants.GOOGLE -> Constants.GOOGLE_AUTHENTICATION;
            case Constants.FACEBOOK -> Constants.FACEBOOK_AUTHENTICATION;
            case Constants.APPLE -> Constants.APPLE_AUTHENTICATION;
            default -> throw new UnknownUserTypeException(Constants.UNKNOWN_USER_TYPE + userType);
        };
    }
}