package com.cordestitch.util;

import java.util.Map;

public class WhatsAppConstants {

    private WhatsAppConstants() {
    }

    public static final String MESSAGE_SENT = "Successfully sent the whatsapp notification";
    public static final String WHATSAPP = "whatsapp";
    public static final String SUBSCRIBE = "subscribe";
    public static final String TEXT = "text";
    public static final String BUTTON = "button";
    public static final String INTERACTIVE = "interactive";
    public static final String WEBHOOK_MESSAGE = "messages";
    public static final String READ = "read";
    public static final String CANCEL_ORDER_BUTTON = "Cancel your order";
    public static final String TRACK_ORDER_BUTTON = "Track your order";
    public static final String YES_BUTTON = "Yes";
    public static final String START_BUTTON = "Start";
    public static final String STOP_BUTTON = "Stop";
    public static final String NO_BUTTON = "No";
    public static final String CANCELLATION_NOT_REQUIRED_MESSAGE = "Thank you for shopping with *cordestitch*! Wishing you a wonderful day ahead.";
    public static final String HEADER_TYPE = "text";
    public static final String REPLY = "reply";
    public static final String LIST = "list";
    public static final String ORDER_CONFIRM_TEMPLATE = "cordestitch_order_confirmation";
    public static final String ORDER_STATUS_SHIPPED_TEMPLATE = "cordestitch_order_status_shipped";
    public static final String SOMETHING_WENT_WRONG_TEMPLATE = "cordestitch_something_went_wrong";
    public static final String ORDER_STATUS_DELIVERED_SUCCESS_TEMPLATE = "cordestitch_order_status_delivered_success";
    public static final String ORDER_STATUS_OUT_FOR_DELIVERED_TEMPLATE = "cordestitch_order_status_delivered";
    public static final String ORDER_STATUS_CANCELLED_TEMPLATE = "cordestitch_order_status_cancelled";
    public static final String ORDER_CANCEL_TEMPLATE = "cordestitch_cancel_order_confirmation";
    public static final String ORDER_DELIVERED_TEMPLATE = "cordestitch_delivery_confirmation";
    public static final String ORDER_CANCEL_FROM_WHATSAPP_TEMPLATE = "cordestitch_order_cancel_from_whatsapp";
    public static final String OTP_TEMPLATE = "cordestitch_otp";
    public static final String EN_US = "en_US";
    public static final String MESSAGE = "/messages";
    public static final String CANCEL_ORDER_INTERACTIVE_HEADER_TEXT = "Help Us Understand Your Cancellation";
    public static final String CANCEL_ORDER_INTERACTIVE_BODY_TEXT = "We’re sorry to hear that you want to cancel your order. Please let us know the reason for the cancellation.";
    public static final String CANCEL_ORDER_ACTION_BODY_TEXT = "Are you sure you want to cancel the order?";
    public static final String CANCEL_ORDER_INTERACTIVE_BUTTON_TEXT = "Cancel My Order";
    public static final String LANGUAGE_POLICY = "deterministic";
    public static final String IMAGE_URL = "https://product-images-2024.s3.ap-south-1.amazonaws.com/HomePage/images/cordestitch.jpeg";
    public static final String TEMPLATE = "template";
    public static final String CANCEL_ORDER = "cancel_order_";
    public static final String QUICK_REPLY = "QUICK_REPLY";
    public static final String CANCEL_APPOINTMENT = "cancel_appointment_";
    public static final String YES_CANCEL_BUTTON = "Yes Cancel";
    public static final String DO_NOT_CANCEL_BUTTON = "Don’t Cancel";
    public static final String CANCEL_APPOINTMENT_BUTTON = "Cancel Appointment";
    public static final String RESCHEDULE_APPOINTMENT_BUTTON = "Reschedule Appointment";
    public static final String RESCHEDULE_APPOINTMENT = "reschedule_appointment_";
    public static final String TRACK_ORDER = "track_order_";
    public static final String SUBSTRING_URL = "url";
    public static final String TITTLE = "tittle";
    public static final String STOP_BUTTON_RESPONSE = "You got it!. \nWe’ll make sure we don’t bother you again If you change your mind, just give us a shout — we’ll be right here, ready to help!\nUntil then, wishing you an awesome day ahead! ";
    public static final String WHILE_CANCELLING_ORDER = "while cancelling order due to a technical issue";
    public static final String TECHNICAL_ISSUE = "due to a technical issue";
    public static final String WELCOME_MESSAGE = "Welcome to *cordestitch*! \nSo glad you're here! Let's make this quick – insane deals,offers, and your favorite products are just a click away! Click ‘Start’ to get started.";
    public static final String MOVE_FORWARD_MESSAGE = "How would you like to proceed? Let us know your next step 👇";
    public static final String TOP_CATEGORY = "Top Categories";
    public static final String TRENDING_PRODUCT = "Trending Products";
    public static final String CONTACT_US = "Contact us";
    public static final String TOP_CATEGORY_INTERACTIVE_BODY_TEXT = "Find what you love in our top categories! ✨";
    public static final String TOP_CATEGORY_INTERACTIVE_BUTTON_TEXT = "View Categories";
    public static final String TOP_CATEGORY_INTERACTIVE_HEADER_TEXT = "Unwrap the Best Categories!";
    public static final String TRENDING_PRODUCT_MESSAGE_BODY = "*Stay Ahead of the Trend!* \nCheck out what’s trending—just one click away from today’s best picks.👇 \nhttps://dev.cordestitch.com";
    public static final String CONTACT_US_MESSAGE_BODY = "*Need Help? We’re Here for You!* \n*Call us*: +919876565433 Or \n*Drop mail*:support@cordestitch.com Or \n*Visit us*: https://dev.cordestitch.com \nWe’re ready to assist you!";
    public static final String IN_APPROPRIATE_SIZE = "Inappropriate Size";
    public static final String ORDERED_BY_MISTAKE = "Order by Mistake";
    public static final String CHANGED_MIND = "Changed Mind";
    public static final String FOUND_BETTER_PRICE = "Found a Better Price";
    public static final String NO_LONGER_NEEDED = "No Longer Needed";
    public static final String FOUND_OTHER = "Found Alternative";
    public static final String WRONG_COLOR = "Wrong Item/Color";
    public static final String QUALITY_CONCERN = "Product Quality Issues";
    public static final String FORMAL_PANTS = "Formal pants";
    public static final String CASUAL_PANTS = "Casual pants";
    public static final String CARGO_PANTS = "Cargo pants";
    public static final String FORMAL_PANT_MESSAGE_BODY = "*Step into Style with Our Formals!* \nDiscover premium comfort and elegance designed to make you stand out at every meeting. \n*Explore now!*👇 \nhttps://dev.cordestitch.com";
    public static final String CASUAL_PANT_MESSAGE_BODY = "*Stay Cool, Stay Casual!* \nFind the perfect pair of casual pants for your everyday vibe—comfortable, stylish, and effortlessly you. \n*Check them out now!.*👇 \nhttps://dev.cordestitch.com";
    public static final String CARGO_PANT_MESSAGE_BODY = "*Built for Comfort, Ready for Action!* \nRugged style with everyday comfort.\nExtra pockets, extra cool—grab yours now!.👇 \nhttps://dev.cordestitch.com";
    public static final Map<String, String> CATEGORY_TYPES = Map.of(
            FORMAL_PANTS, FORMAL_PANT_MESSAGE_BODY,
            CASUAL_PANTS, CASUAL_PANT_MESSAGE_BODY,
            CARGO_PANTS, CARGO_PANT_MESSAGE_BODY
    );
}