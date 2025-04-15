package com.cordestitch.controller.customization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.customization.ButtonTypes;
import com.cordestitch.entity.customization.CustomizationAttribute;
import com.cordestitch.entity.customization.PocketTypes;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.customization.admincustomization.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizationResponse;
import com.cordestitch.service.service.customization.CustomizationService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CustomizationController.class)
class CustomizationControllerTest {
    @MockBean
    public CustomizationService customizationService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }

    @Test
    void testAddData() throws Exception{
        CustomizationRequest request=getCustomizationRequest();
        when(customizationService.addData(request)).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/customization/addCustomizationData")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCustomizationType() throws Exception{
        String pantType="formals";
        when(customizationService.getCustomizationType(pantType)).thenReturn(new CustomizationResponse());
        mockMvc.perform(get("/api/customization/getCustomizationType")
                        .param("pantType",pantType)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCustomizationData() throws Exception{
        UpdateCustomizationRequest request=getUpdateCustomizationRequest();
        when(customizationService.updateCustomizationData(request)).thenReturn(new SuccessResponse());
        mockMvc.perform(put("/api/customization/updateCustomizationData")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddFabricData() throws Exception{
        AddFabricRequest request=getAddFabricRequest();
        when(customizationService.addFabricData(request)).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/customization/addFabricData")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateFabricData() throws Exception{
        UpdateFabricRequestList request=getUpdateFabricRequestList();
        when(customizationService.updateFabricData(request)).thenReturn(new SuccessResponse());
        mockMvc.perform(put("/api/customization/updateFabricData")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteFabricData() throws Exception{
        String customizationId="customId10";
        String fabricId="fab1";
        when(customizationService.deleteFabricData(any(),any())).thenReturn(new SuccessResponse());
        mockMvc.perform(delete("/api/customization/deleteFabricData")
                        .param("customizationId",customizationId)
                        .param("fabricId",fabricId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCustomizationData() throws Exception{
        String customizationId = "customId123";
        when(customizationService.deleteCustomizationData(customizationId)).thenReturn(new SuccessResponse());
        mockMvc.perform(delete("/api/customization/deleteCustomizationData")
                .param("customizationId",customizationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUploadFabricFiles() throws Exception{
        String pantType="formals";
        String fabricId="fab1";
        MockMultipartFile images=new MockMultipartFile("images","img.jpg",MediaType.IMAGE_JPEG_VALUE,"image content".getBytes());
        when(customizationService.uploadFabricFiles(any(),any(),any())).thenReturn(new SuccessResponse());
        mockMvc.perform(multipart("/api/customization/uploadFabricFiles")
                        .file(images)
                        .param("pantType",pantType)
                        .param("fabricId",fabricId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUploadImages() throws Exception{
        String pantType="formals";
        String attributeType="attribute1";
        AttributeTypeRequestList requestList=getAttributeTypeRequestList();
        when(customizationService.uploadFabricFiles(any(),any(),any())).thenReturn(new SuccessResponse());
        mockMvc.perform(multipart("/api/customization/uploadCustomizationAttributeImages")
                        .param("pantType",pantType)
                        .param("attributeType",attributeType)
                        .flashAttr("requestList",requestList)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private AttributeTypeRequestList getAttributeTypeRequestList() {
        AttributeTypeRequestList requestList=new AttributeTypeRequestList();
        requestList.setAttributeRequests(List.of(getAttributeTypeRequest()));
        return requestList;
    }

    private AttributeTypeRequest getAttributeTypeRequest() {
        AttributeTypeRequest request=new AttributeTypeRequest();
        MultipartFile[] images={new MockMultipartFile("images","img.jpg",MediaType.IMAGE_JPEG_VALUE,"image content".getBytes())};
        request.setAttributeId("attribute1");
        request.setImages(images);
        return request;
    }


    private UpdateFabricRequestList getUpdateFabricRequestList() {
        UpdateFabricRequestList requestList=new UpdateFabricRequestList();
        requestList.setCustomizationId("customId02");
        requestList.setFabricsRequestList(getUpdatedListFabricRequest());
        return requestList;
    }

    private AddFabricRequest getAddFabricRequest() {
        AddFabricRequest fabricRequest=new AddFabricRequest();
        fabricRequest.setCustomizationId("customId01");
        fabricRequest.setFabricRequestList(getListFabricRequest());
        return fabricRequest;
    }

    private UpdateCustomizationRequest getUpdateCustomizationRequest() {
        UpdateCustomizationRequest customizationRequest=new UpdateCustomizationRequest();
        customizationRequest.setCustomizationId("updatedId1");
        customizationRequest.setCustomizationImages(getCustomizationImages());
        customizationRequest.setFabric(getUpdatedListFabricRequest());
        customizationRequest.setFlyType(getListFlyType());
        customizationRequest.setFitType(getListFitType());
        customizationRequest.setButtonTypes(getButtonTypes());
        customizationRequest.setPantType("formal");
        customizationRequest.setRiseType(getRiseType());
        customizationRequest.setPantInSeamLength(List.of(15,9,6));
        customizationRequest.setPantOutSeamLength(List.of(13,8,5));
        customizationRequest.setPantPleatsType(getPantPleatsType());
        customizationRequest.setPocketTypes(getPocketTypes());
        customizationRequest.setTrueWaistMeasurement(List.of(30,32,34,36));
        customizationRequest.setPantCuffType(getPantCuffsType());
        return customizationRequest;
    }

    private List<UpdateFabricRequest> getUpdatedListFabricRequest() {
        List<UpdateFabricRequest> list=new ArrayList<>();
        UpdateFabricRequest request=new UpdateFabricRequest();
        request.setFabricColor("blue");
        request.setFabricId("fab1");
        request.setFabricPrice(150.0);
        request.setFabricDescription("WrinkleFree cotton pants");
        request.setFabricColorCode("#0000FF");
        request.setProductOfferPercentage(5.0);
        list.add(request);
        return list;
    }

    private CustomizationRequest getCustomizationRequest() {
        CustomizationRequest customizationRequest=new CustomizationRequest();
        customizationRequest.setCustomizationImages(getCustomizationImages());
        customizationRequest.setFabric(getListFabricRequest());
        customizationRequest.setFlyType(getListFlyType());
        customizationRequest.setFitType(getListFitType());
        customizationRequest.setButtonTypes(getButtonTypes());
        customizationRequest.setPantCuffsType(getPantCuffsType());
        customizationRequest.setPantType("formal");
        customizationRequest.setRiseType(getRiseType());
        customizationRequest.setPantInSeamLength(List.of(15,9,6));
        customizationRequest.setPantOutSeamLength(List.of(13,8,5));
        customizationRequest.setPantPleatsType(getPantPleatsType());
        customizationRequest.setPocketTypes(getPocketTypes());
        customizationRequest.setTrueWaistMeasurement(List.of(30,32,34,36));
        return customizationRequest;
    }

    private PocketTypes getPocketTypes() {
        PocketTypes pocketTypes=new PocketTypes();
        pocketTypes.setBackPocketTypes(getBackPocketTypes());
        pocketTypes.setSidePocketTypes(getSidePocketTypes());
        pocketTypes.setPocketTypeImagesUrl(getPocketTypeImagesUrl());
        return pocketTypes;
    }

    private List<CustomizationAttribute> getPocketTypeImagesUrl() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("PocketTypeImageUrl");
        customizationAttribute.setAttributeId("PocketTypeImageUrl1");
        customizationAttribute.setAttributeImagesUrl(List.of("PocketTypeImg1Url","PocketTypeImg2Url"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getSidePocketTypes() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("sidePocketType");
        customizationAttribute.setAttributeId("sidePocketType1");
        customizationAttribute.setAttributeImagesUrl(List.of("sidePocketTypeImg1","sidePocketTypeImg2"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getBackPocketTypes() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("backPocketType");
        customizationAttribute.setAttributeId("backPocketType1");
        customizationAttribute.setAttributeImagesUrl(List.of("backPocketTypeImg1","backPocketTypeImg2"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getPantPleatsType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("pantPleatsType");
        customizationAttribute.setAttributeId("pantPleatsType1");
        customizationAttribute.setAttributeImagesUrl(List.of("pantPleatsTypeValue1","panatPleatsTypeValue2"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getRiseType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("riseType");
        customizationAttribute.setAttributeId("riseType1");
        customizationAttribute.setAttributeImagesUrl(List.of("riseTypeValue1","riseTypeValue2"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getPantCuffsType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("pantCuffsTypes");
        customizationAttribute.setAttributeId("pantCuffsTypes1");
        customizationAttribute.setAttributeImagesUrl(List.of("pantCuffsTypesValue1","pantCuffsTypesValue2"));
        list.add(customizationAttribute);
        return list;
    }

    private ButtonTypes getButtonTypes() {
        ButtonTypes buttonTypes=new ButtonTypes();
        buttonTypes.setFrontButtonTypes(getListFrontButtonType());
        buttonTypes.setBackButtonTypes(getListBackButtonType());
        buttonTypes.setButtonTypeImagesUrl(getListButtonTypeImagesUrl());
        return buttonTypes;
    }

    private List<CustomizationAttribute> getListButtonTypeImagesUrl() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("buttonTypeImagesUrl");
        customizationAttribute.setAttributeId("buttonTypeImagesUrl1");
        customizationAttribute.setAttributeImagesUrl(List.of("buttonTypeImagesUrl1.com","buttonTypeImagesUrl2.com"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getListBackButtonType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("back");
        customizationAttribute.setAttributeId("back1");
        customizationAttribute.setAttributeImagesUrl(List.of("backImg1.com","backImg2.com"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getListFrontButtonType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("front");
        customizationAttribute.setAttributeId("front1");
        customizationAttribute.setAttributeImagesUrl(List.of("frontImg1.com","frontImg2.com"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getListFitType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("fit");
        customizationAttribute.setAttributeId("fit1");
        customizationAttribute.setAttributeImagesUrl(List.of("fitImg1.com","fitImg2.com"));
        list.add(customizationAttribute);
        return list;
    }

    private List<CustomizationAttribute> getListFlyType() {
        List<CustomizationAttribute> list=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeType("fly");
        customizationAttribute.setAttributeId("fly1");
        customizationAttribute.setAttributeImagesUrl(List.of("flyImg1.com","flyImg2.com"));
        list.add(customizationAttribute);
        return list;
    }

    private List<FabricRequest> getListFabricRequest() {
        List<FabricRequest> list=new ArrayList<>();
        FabricRequest fabricRequest=new FabricRequest();
        fabricRequest.setFabricColor("white");
        fabricRequest.setFabricDescription("soft and wrinkleFree");
        fabricRequest.setFabricPrice(100.0);
        fabricRequest.setFabricColorCode("#000000");
        fabricRequest.setProductOfferPercentage(5.0);
        list.add(fabricRequest);
        return list;
    }

    private List<CustomizationAttribute> getCustomizationImages() {
        List<CustomizationAttribute> customizationAttributes=new ArrayList<>();
        CustomizationAttribute customizationAttribute=new CustomizationAttribute();
        customizationAttribute.setAttributeId("attribute1");
        customizationAttribute.setAttributeType("type1");
        customizationAttribute.setAttributeImagesUrl(List.of("img1.com","img2.com"));
        customizationAttributes.add(customizationAttribute);
        return customizationAttributes;
    }


}