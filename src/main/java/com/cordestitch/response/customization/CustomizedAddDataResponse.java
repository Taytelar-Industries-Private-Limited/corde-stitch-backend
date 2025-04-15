package com.cordestitch.response.customization;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class CustomizedAddDataResponse {

    private String pantType;

    private Integer trueWaistMeasurement;

    private Integer pantInSeamLength;

    private Integer pantOutSeamLength;

    private String fitType;

    private String riseType;

    private String frontPocketType;

    private String backPocketType;

    private String frontButtonType;

    private String backButtonType;

    private String pantPleatType;

    private String flyType;

    private String pantCuffsType;

    @Field("fabric_details")
    private JsonNode fabric;
}