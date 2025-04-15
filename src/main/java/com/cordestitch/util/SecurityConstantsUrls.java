package com.cordestitch.util;

public class SecurityConstantsUrls {

    private SecurityConstantsUrls() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    protected static final String[] WHITE_LIST_URLS = {
            "/",
            "/favicon.ico",
            "/actuator/health",
            "/actuator/caches",
            "/actuator/info",
            "/actuator/prometheus",
            "/api/auth/authRegisterAndLogin",
            "/api/jwt/refreshToken",
            "/api/user/login",
            "/api/otp/sendOtp",
            "/api/otp/verifyOtp",
            "/api/jwt/getAccessToken",
            "/api/jwt/sessionStatus",
            "/api/product/getAllProducts",
            "/api/product/getProductByProductId",
            "/api/home/getAllHomePageImages",
            "/api/product/filterProductData",
            "/api/product/getAllFilterCondition",
            "/api/customization/getCustomizationType",
            "/api/reviews/getProductReviews",
            "/api/payment/reconcilePayments",
            "/api/payment/refund-status-checking",
            "/api/payment/initiate-refund",
            "/api/reviews/filterReviews",
            "/api/faqs/get-all-faqs",
            "/api/cache/flushCache",
            "/api/reviews/filterReviews",
            "/api/pdf/report",
            "/api/pdf/productGetReport",
            "/api/pdf/deliveredOrderReport",
            "/api/pdf/confirmedOrdersReport",
            "/api/pdf/cancelledOrdersReport",
            "/api/pdf/returnOrderReport",
            "/api/loyalty/processLoyaltyPointsToAccount",
            "/api/session/generate",
            "/api/session/cart/add",
            "/api/session/cart/fetch",
            "/api/session/cart/update",
            "/api/session/cart/remove",
            "/api/session/cart/delete-items",
            "/api/cache/flushCache",
            "/api/otp/sendWhatsAppOtp",
            "/api/webhook"
    };

    protected static final String[] CUSTOMER_LIST_URLS = {
            "/api/user/logout",
            "/api/order/placeAnOrder",
            "/api/order/cancelOrder",
            "/api/order/returnOrder",
            "/api/order/getAllOrders",
            "/api/order/getOrderDetailsByOrderId",
            "/api/user/addAddress",
            "/api/user/deleteAddress",
            "/api/user/getAddresses",
            "/api/user/updateAddress",
            "/api/auth/updateEmailAddressOrPhoneNumber",
            "/api/auth/validateOTPForEmailOrPhoneNumber",
            "/api/auth/checkEmailOrPhoneNumberVerified",
            "/api/auth/updateProfile",
            "/api/auth/getUserDetails",
            "/api/auth/updateUserDetails",
            "/api/user/addBankDetails",
            "/api/user/getAllBankDetails",
            "/api/user/deleteBankDetails",
            "/api/payment/createPayment",
            "/api/payment/verifySignature",
            "/api/payment/retryPayment",
            "/api/payment/transfer-to-account",
            "/api/cart/addToCart",
            "/api/cart/updateCartItem",
            "/api/cart/getCartItems",
            "/api/cart/deleteCartItem",
            "/api/userCustomization/addCustomizedDataToCart",
            "/api/userCustomization/deleteCustomizedCartItem",
            "/api/userCustomization/updateCustomizedCartItem",
            "/api/reviews/validateUserPurchase",
            "/api/reviews/createReview",
            "/api/reviews/updateReview",
            "/api/reviews/deleteReview",
            "/api/loyalty/getLoyaltyPointsSummary",
            "/api/loyalty/processReferral"
    };

    protected static final String[] ADMIN_LIST_URLS = {
            "/api/home/uploadHomePageImages",
            "/api/product/addProduct",
            "/api/product/uploadFiles",
            "/api/product/updateProduct",
            "/api/customization/addCustomizationData",
            "/api/customization/uploadCustomizationAttributeImages",
            "/api/customization/uploadFabricFiles",
            "/api/customization/addFabricData",
            "/api/customization/deleteFabricData",
            "/api/customization/updateFabricData",
            "/api/customization/deleteCustomizationData",
            "/api/customization/updateCustomizationData",
            "/api/faqs/add-faqs",
            "/api/faqs/update-faqs",
            "/api/faqs/delete-faqs",
            "/api/order/check-returned-product"
    };

    public static String[] getWhiteListUrls() {
        return WHITE_LIST_URLS;
    }

    public static String[] getAdminUrls() {
        return ADMIN_LIST_URLS;
    }

    public static String[] getCustomerUrls() {
        return CUSTOMER_LIST_URLS;
    }
}
