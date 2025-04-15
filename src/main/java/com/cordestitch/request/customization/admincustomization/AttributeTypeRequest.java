package com.cordestitch.request.customization.admincustomization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class AttributeTypeRequest {

    @NotBlank(message = "Attribute Type Request cannot be blank")
    private String attributeId;

    @NotNull(message = "Images cannot be null")
    private MultipartFile [] images;
}