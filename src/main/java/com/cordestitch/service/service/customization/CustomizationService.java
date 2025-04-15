package com.cordestitch.service.service.customization;

import com.cordestitch.request.customization.admincustomization.*;
import com.cordestitch.request.customization.admincustomization.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomizationService {

    SuccessResponse addData(CustomizationRequest request);

    CustomizationResponse getCustomizationType(String pantType);

    SuccessResponse updateCustomizationData(UpdateCustomizationRequest request);

    SuccessResponse addFabricData(AddFabricRequest request);

    SuccessResponse deleteFabricData(String customizationId, String fabricId);

    SuccessResponse deleteCustomizationData(String customizationId);

    SuccessResponse updateFabricData(UpdateFabricRequestList request);

    SuccessResponse uploadFabricFiles(String pantType, String fabricId, MultipartFile[] images);

    SuccessResponse uploadCustomizationAttributeImages(String pantType,String attributeType, List<AttributeTypeRequest> request);
}
