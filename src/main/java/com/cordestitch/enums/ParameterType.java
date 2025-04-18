package com.cordestitch.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParameterType {

    TEXT("text"),
    DATE_TIME("date_time"),
    PAYLOAD("payload"),
    IMAGE("image");

    private final String type;
}