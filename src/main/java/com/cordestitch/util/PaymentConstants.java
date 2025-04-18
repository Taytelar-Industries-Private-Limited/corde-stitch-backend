package com.cordestitch.util;

public class PaymentConstants {
    public static final String CONTACT_ACTIVE_DEACTIVATE_URL = "https://api.razorpay.com/v1/contacts/";
    public static final String ACTIVE = "active";
    public static final String FUND_ACCOUNT_ACTIVE_DEACTIVATE_URL = "https://api.razorpay.com/v1/fund_accounts/";
    public static final String PAYOUT_REFUND_CHECKING_URL = "https://api.razorpay.com/v1/payouts/";
    public static final String CASHBACK = "cashback";
    public static final String MODE = "mode";
    public static final String NEFT = "NEFT";
    public static final String QUEUE_IF_LOW_BALANCE = "queue_if_low_balance";
    public static final String ACTIVATE_CONTACT_ERR_MSG = "Failed to activate Razorpay contact";
    public static final String DEACTIVATE_CONTACT_ERR_MSG = "Failed to deactivate Razorpay contact";
    public static final String DEACTIVATE_FUND_ACCOUNT_ERR_MSG = "Error deactivating Razorpay fund account";
    public static final String ACTIVATE_FUND_ACCOUNT_ERR_MSG = "Error activating Razorpay fund account";
    public static final String FUND_ACCOUNT_DEACTIVATION_FAILED = "Failed to deactivate Razorpay Fund Account";
    public static final String FUND_ACCOUNT_ACTIVATION_FAILED = "Failed to activate Razorpay fund account";
    public static final String CONTACT_DEACTIVATED_FAILED = "Failed to deactivate Razorpay Contact";
    public static final String CONTACT_ACTIVATED_FAILED = "Failed to activate Razorpay contact";

    private PaymentConstants() {

    }
    public static final String PAYOUT_URL = "https://api.razorpay.com/v1/payouts";
    public static final String CONTACT_URL = "https://api.razorpay.com/v1/contacts";
    public static final String ALGORITHM = "HmacSHA256";
    public static final String STATUS = "status";
    public static final String CAPTURED = "captured";
    public static final String PROCESSED = "processed";
    public static final String AMOUNT = "amount";
    public static final String ID = "id";
    public static final String PAYMENT_ID = "payment_id";
    public static final String ACCOUNT_NUMBER = "account_number";
    public static final String FUND_ACCOUNT_ID = "fund_account_id";
    public static final String CURRENCY = "currency";
    public static final String INR = "INR";
    public static final String PURPOSE = "purpose";
    public static final String REFUND = "refund";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String CONTACT = "contact";
    public static final String TYPE = "type";
    public static final String CUSTOMER = "customer";
    public static final String CONTACT_ID = "contact_id";
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String BANK_ACCOUNT_TYPE = "bank_account";
    public static final String IFSC = "ifsc";
    public static final String NOTES = "notes";
    public static final String RECEIPT = "receipt";
}
