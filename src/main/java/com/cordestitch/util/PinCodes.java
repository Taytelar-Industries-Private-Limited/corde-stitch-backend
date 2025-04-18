package com.cordestitch.util;

import java.util.Arrays;
import java.util.List;

public class PinCodes {

    private PinCodes() {
    }
    protected static final List<String> BENGALURU_PIN_CODES = Arrays.asList(
        "560001", "560002", "560003", "560004", "560005",
        "560006", "560007", "560008", "560009", "560010",
        "560011", "560012", "560013", "560014", "560015",
        "560016", "560017", "560018", "560019", "560020"
    );

    public static boolean isPinCodeInBengaluru(String pinCode) {
        return BENGALURU_PIN_CODES.contains(pinCode);
    }
}
