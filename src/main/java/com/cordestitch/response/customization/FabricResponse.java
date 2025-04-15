package com.cordestitch.response.customization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FabricResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String fabricId;
    private String fabricColor;
    private String fabricColorCode;
    private String fabricDescription;
    private Double fabricPrice;
    private Double productOfferPercentage;
    private List<String> imageUrl;
}
