package com.cordestitch.request.customization.usercustomization;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
public class CustomizedAddDataRequest {

    @NotBlank(message = "Pant type cannot be blank")
    private String pantType;

    @NotNull(message = "True waist measurement cannot be null")
    @Positive(message = "True waist measurement must be positive")
    private Integer trueWaistMeasurement;

    @NotNull(message = "Pant in-seam length cannot be null")
    @Positive(message = "Pant in-seam length must be positive")
    private Integer pantInSeamLength;

    @NotNull(message = "Pant out-seam length cannot be null")
    @Positive(message = "Pant out-seam length must be positive")
    private Integer pantOutSeamLength;

    @NotBlank(message = "Fit type cannot be blank")
    private String fitType;

    @NotBlank(message = "Rise type cannot be blank")
    private String riseType;

    @NotBlank(message = "Front pocket type cannot be blank")
    private String frontPocketType;

    @NotBlank(message = "Back pocket type cannot be blank")
    private String backPocketType;

    @NotBlank(message = "Front button type cannot be blank")
    private String frontButtonType;

    @NotBlank(message = "Back button type cannot be blank")
    private String backButtonType;

    @NotBlank(message = "Pleat type cannot be blank")
    private String pantPleatType;

    @NotBlank(message = "Fly type cannot be blank")
    private String flyType;

    @NotNull(message = "Pant cuff type cannot be null")
    private String pantCuffsType;

    @NotNull(message = "Fabric details cannot be null")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode fabricDetails;
}