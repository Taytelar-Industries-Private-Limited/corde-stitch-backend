package com.cordestitch.entity.customization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Fabric {

    private String fabricId;
    private String fabricColor;
    private String fabricColorCode;
    private String fabricDescription;
    private Double fabricPrice;
    private Double productOfferPercentage;
    private List<String> imageUrl;
}
