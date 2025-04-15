package com.cordestitch.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComponentType {

    HEADER("header"),
    BODY("body"),
    FOOTER("footer"),
    BUTTON("button");

    private final String type;
}