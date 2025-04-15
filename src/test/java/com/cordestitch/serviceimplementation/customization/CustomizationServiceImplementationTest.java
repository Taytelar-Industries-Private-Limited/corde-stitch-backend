package com.cordestitch.serviceimplementation.customization;


import com.cordestitch.service.serviceimplementation.customization.CustomizationServiceImplementation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.entity.customization.*;
import com.cordestitch.exception.customization.*;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.repository.customization.CustomizationRepository;
import com.cordestitch.request.customization.admincustomization.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizationResponse;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

class CustomizationServiceImplementationTest {

    @InjectMocks
    private CustomizationServiceImplementation customizationServiceImplementation;

    @Mock
    private CustomizationRepository customizationRepository;

    @Mock
    private Generator generator;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CustomizationEntity customizationEntityABC;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void addData_Success(){
        CustomizationRequest request=getCustomizationRequest();
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.empty());
        SuccessResponse response = customizationServiceImplementation.addData(request);
        assertEquals(Constants.CUSTOMIZATION_DATA_ADDED_SUCCESSFULLY + request.getPantType(), HttpStatus.OK.value(),response.getStatusCode());
    }

    @Test
    void addData_Success_When_Fabric_ImageUrl_Is_Empty(){
        CustomizationRequest request=getCustomizationRequest();
        request.getFabric().getFirst().setImageUrl(List.of());
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.empty());
        SuccessResponse response = customizationServiceImplementation.addData(request);
        assertEquals(Constants.CUSTOMIZATION_DATA_ADDED_SUCCESSFULLY + request.getPantType(), HttpStatus.OK.value(),response.getStatusCode());
    }

    @Test
    void addData_Success_When_AttributeImageUrl_Is_Not_Empty(){
        CustomizationRequest request=getCustomizationRequest();
        request.getCustomizationImages().getFirst().setAttributeImagesUrl(List.of());
        request.getRiseType().getFirst().setAttributeImagesUrl(List.of());
        request.getPantPleatsType().getFirst().setAttributeImagesUrl(List.of());
        request.getPantCuffsType().getFirst().setAttributeImagesUrl(List.of());
        request.getFitType().getFirst().setAttributeImagesUrl(List.of());
        request.getFlyType().getFirst().setAttributeImagesUrl(List.of());
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.empty());
        SuccessResponse response = customizationServiceImplementation.addData(request);
        assertEquals(Constants.CUSTOMIZATION_DATA_ADDED_SUCCESSFULLY + request.getPantType(), HttpStatus.OK.value(),response.getStatusCode());
    }

    @Test
    void addData_Exception() throws Exception{
        CustomizationRequest request=getCustomizationRequest();
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.of(getCustomizationEntity()));
        CustomizationTypeAlreadyExistException exception=assertThrows(CustomizationTypeAlreadyExistException.class, ()->customizationServiceImplementation.addData(request));
        assertEquals(Constants.CUSTOMIZATION_TYPE_ERROR, Constants.CUSTOMIZATION_TYPE_ERROR, exception.getMessage());
    }

    @Test
    void getCustomizationType_Success() throws Exception{
        String pantType = "Slim Fit";
        Optional<CustomizationEntity> customEntity = Optional.of(getCustomizationEntityAA());
        when(customizationRepository.findByPantType(pantType)).thenReturn(customEntity);
        when(modelMapper.map(customEntity, CustomizationResponse.class)).thenReturn(new CustomizationResponse());
        when(customizationServiceImplementation.getCustomizationType(pantType)).thenReturn(new CustomizationResponse());
        verify(customizationRepository, times(1)).findByPantType(pantType);
    }

    @Test
    void getCustomizationType_Exception_Customization_Type_Not_Found(){
        String pantType="formal";
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.getCustomizationType(pantType));
        assertEquals(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType,Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType,exception.getMessage());
    }

    @Test
    void getCustomizationType_Exception() throws Exception{
        String pantType="formal";
        CustomizationEntity customizationEntity=getCustomizationEntity();
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.of(customizationEntity));
        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.getCustomizationType(pantType));
    }

    
    @Test
    void updateCustomizationData_Success() throws Exception{
        UpdateCustomizationRequest request=getUpdateCustomizationRequest();
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        SuccessResponse response=customizationServiceImplementation.updateCustomizationData(request);
        assertEquals(Constants.CUSTOMIZATION_DATA_UPDATED_SUCCESSFULLY + request.getPantType(),HttpStatus.OK.value(),response.getStatusCode());
    }

    @Test
    void updateCustomizationData_Exception_Convert_From_JSON_Error() throws Exception{
        UpdateCustomizationRequest request=getUpdateCustomizationRequest();
        CustomizationEntity customizationEntity=getCustomizationEntityAA();
        ObjectMapper mapper=new ObjectMapper();
        customizationEntity.setFitType(mapper.readTree("[{\"Id\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        Optional<CustomizationEntity> entity=Optional.of(customizationEntity);
        when(customizationRepository.findByCustomizationId(any())).thenReturn(entity);
        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.updateCustomizationData(request));
    }

    @Test
    void updateCustomizationData_Exception_Customization_Type_Not_Found(){
        UpdateCustomizationRequest request=getUpdateCustomizationRequest();
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.updateCustomizationData(request));
        Assertions.assertEquals(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + request.getCustomizationId(),exception.getMessage());
    }

    @Test
    void addFabricData_Success() throws Exception{
        AddFabricRequest request=getAddFabricRequest();
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        SuccessResponse response=customizationServiceImplementation.addFabricData(request);
        Assertions.assertEquals(Constants.FABRIC_ADDED_SUCCESSFULLY + getCustomizationEntityAA().getPantType(),response.getMessage());
    }

    @Test
    void addFabricData_Exception_Convert_From_JSON_Error() throws Exception{
        AddFabricRequest request=getAddFabricRequest();
        CustomizationEntity customizationEntity=getCustomizationEntityAA();
        ObjectMapper mapper=new ObjectMapper();
        customizationEntity.setFitType(mapper.readTree("[{\"Id\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        Optional<CustomizationEntity> entity=Optional.of(customizationEntity);
        when(customizationRepository.findByCustomizationId(any())).thenReturn(entity);
        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.addFabricData(request));
    }

    @Test
    void addFabricData_Exception_Customization_Type_Not_Found(){
        AddFabricRequest request=getAddFabricRequest();
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.addFabricData(request));
        Assertions.assertEquals(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + request.getCustomizationId(),exception.getMessage());
    }

    @Test
    void deleteFabricData_Success() throws Exception{
        String customizationId = "custom1";
        String fabricId = "1";
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        SuccessResponse response=customizationServiceImplementation.deleteFabricData(customizationId, fabricId);
        Assertions.assertEquals(Constants.FABRIC_DELETED_SUCCESSFULLY + getCustomizationEntityAA().getPantType(),response.getMessage());
    }

    @Test
    void deleteFabricData_Exception_Fabric_Not_Found() throws Exception{
        String customizationId = "custom1";
        String fabricId = "fabric1";
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        FabricNotFoundException exception=assertThrows(FabricNotFoundException.class, ()->customizationServiceImplementation.deleteFabricData(customizationId, fabricId));
        Assertions.assertEquals(Constants.FABRIC_NOT_FOUND_ERROR + fabricId,exception.getMessage());
    }

    @Test
    void deleteFabricData_Exception_Convert_From_JSON_Error() throws Exception{
        String customizationId = "custom1";
        String fabricId = "fabric1";
        CustomizationEntity customizationEntity=getCustomizationEntityAA();
        ObjectMapper mapper=new ObjectMapper();
        customizationEntity.setFitType(mapper.readTree("[{\"Id\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        Optional<CustomizationEntity> entity=Optional.of(customizationEntity);
        when(customizationRepository.findByCustomizationId(any())).thenReturn(entity);
        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.deleteFabricData(customizationId,fabricId));
    }

    @Test
    void deleteFabricData_Exception_Customization_Type_Not_Found(){
        String customizationId = "custom1";
        String fabricId = "fabric1";
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.deleteFabricData(customizationId,fabricId));
        Assertions.assertEquals(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + customizationId,exception.getMessage());
    }

    @Test
    void deleteCustomizationData_Success() throws Exception{
        String customizationId = "customId1";
        when(customizationRepository.findById(any())).thenReturn(Optional.of(getCustomizationEntity()));
        customizationRepository.deleteById(customizationId);
        SuccessResponse response = customizationServiceImplementation.deleteCustomizationData(customizationId);
        Assertions.assertEquals(Constants.CUSTOMIZATION_DATA_DELETED,response.getMessage());
    }

    @Test
    void deleteCustomizationData_Exception(){
        String customizationId = "customId1";
        when(customizationRepository.findById(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.deleteCustomizationData(customizationId));
        Assertions.assertEquals(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + customizationId,exception.getMessage());
    }

    @Test
    void updateFabricData_Success() throws Exception{
        UpdateFabricRequestList requestList = getUpdateFabricRequestList();
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        SuccessResponse response=customizationServiceImplementation.updateFabricData(requestList);
        Assertions.assertEquals(Constants.FABRIC_UPDATED_SUCCESSFULLY + getCustomizationEntityAA().getPantType(),response.getMessage());
    }

    @Test
    void updateFabricData_Success_When_Request_Is_Not_Null() throws Exception{
        UpdateFabricRequestList requestList = getUpdateFabricRequestList();
        requestList.getFabricsRequestList().getFirst().setFabricId("1");
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        SuccessResponse response=customizationServiceImplementation.updateFabricData(requestList);
        Assertions.assertEquals(Constants.FABRIC_UPDATED_SUCCESSFULLY + getCustomizationEntityAA().getPantType(),response.getMessage());
    }

    @Test
    void updateFabricData_Exception_Convert_From_JSON_Error() throws Exception{
        UpdateFabricRequestList requestList = getUpdateFabricRequestList();
        CustomizationEntity customizationEntity=getCustomizationEntityAA();
        ObjectMapper mapper=new ObjectMapper();
        customizationEntity.setFitType(mapper.readTree("[{\"Id\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        Optional<CustomizationEntity> entity=Optional.of(customizationEntity);
        when(customizationRepository.findByCustomizationId(any())).thenReturn(entity);
        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.updateFabricData(requestList));
    }

    @Test
    void updateFabricData_Exception_Customization_Type_Not_Found(){
        UpdateFabricRequestList requestList = getUpdateFabricRequestList();
        when(customizationRepository.findByCustomizationId(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.updateFabricData(requestList));
        Assertions.assertEquals(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + requestList.getCustomizationId(),exception.getMessage());
    }


    @Test
    void uploadFabricFiles_Success() throws Exception {
        String pantType = "formal";
        String fabricId = "1";
        String bucketName = "test-bucket";

        Field bucketNameField = CustomizationServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(customizationServiceImplementation, bucketName);
        CustomizationEntity mockEntity = getCustomizationEntityAA();
        when(customizationRepository.findByPantType(pantType)).thenReturn(Optional.of(mockEntity));

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        MultipartFile[] images = {image1, image2};

        S3Utilities mockS3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(mockS3Utilities);

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/formal/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/formal/images/image2.jpg";

        when(mockS3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL(imageUrl1))
                .thenReturn(new URL(imageUrl2));
        SuccessResponse response = customizationServiceImplementation.uploadFabricFiles(pantType, fabricId, images);
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(Constants.IMAGES_AND_VIDEO_ADDED_SUCCESSFULLY, response.getMessage());

        verify(customizationRepository).save(mockEntity);
    }

    @Test
    void uploadFabricFiles_ShouldThrowS3UploadException_WhenIOExceptionOccurs() throws Exception {
        String pantType = "formal";
        String fabricId = "1";
        CustomizationEntity mockEntity = getCustomizationEntityAA();
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.of(mockEntity));

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        when(image1.getInputStream()).thenThrow(new IOException("Error uploading file to S3: {}"));

        MultipartFile[] images = {image1, image2};
        S3UploadException exception = assertThrows(S3UploadException.class,
                () -> customizationServiceImplementation.uploadFabricFiles(pantType, fabricId, images));

        Assertions.assertEquals(Constants.UPLOAD_ERROR, exception.getMessage());

        verify(customizationRepository, never()).save(any());

    }

    @Test
    void uploadFabricFiles_Exception_Fabric_Not_Found_Error() throws Exception{
        String pantType = "formal";
        String fabricId = "fabric1";
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");
        MultipartFile[] images = {image1, image2};
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.of(getCustomizationEntityAA()));
        FabricNotFoundException exception = assertThrows(FabricNotFoundException.class, ()->customizationServiceImplementation.uploadFabricFiles(pantType,fabricId,images));
        Assertions.assertEquals(Constants.FABRIC_NOT_FOUND_ERROR + fabricId,exception.getMessage());
    }

    @Test
    void uploadFabricFiles_Exception_Convert_From_JSON_Error() throws Exception{
        String pantType = "formal";
        String fabricId = "1";
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");
        MultipartFile[] images = {image1, image2};
        CustomizationEntity customizationEntity=getCustomizationEntityAA();
        ObjectMapper mapper=new ObjectMapper();
        customizationEntity.setFitType(mapper.readTree("[{\"Id\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        Optional<CustomizationEntity> entity=Optional.of(customizationEntity);
        when(customizationRepository.findByPantType(any())).thenReturn(entity);
        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.uploadFabricFiles(pantType,fabricId,images));
    }

    @Test
    void uploadFabricFiles_Exception_Customization_Type_Not_Found(){
        String pantType = "formal";
        String fabricId = "1";
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");
        MultipartFile[] images = {image1, image2};
        when(customizationRepository.findByPantType(any())).thenReturn(Optional.empty());
        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.uploadFabricFiles(pantType,fabricId,images));
        Assertions.assertEquals(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType,exception.getMessage());
    }

    @Test
    void uploadCustomizationAttributeImages_Success() throws Exception {
        String pantType = "formal";
        String attributeType = "attribute";
        String bucketName = "test-bucket";

        Field bucketNameField = CustomizationServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(customizationServiceImplementation, bucketName);

        CustomizationEntity mockEntity = getCustomizationEntityAA();
        when(customizationRepository.findByPantType(pantType)).thenReturn(Optional.of(mockEntity));


        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        MultipartFile[] images = {image1, image2};
        List<AttributeTypeRequest> request = getAttributeTypeRequest();
        request.getFirst().setImages(images);

        S3Utilities mockS3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(mockS3Utilities);

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/formal/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/formal/images/image2.jpg";

        when(mockS3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL(imageUrl1))
                .thenReturn(new URL(imageUrl2));

        SuccessResponse response = customizationServiceImplementation.uploadCustomizationAttributeImages(pantType, attributeType, request);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(Constants.IMAGES_ADDED_SUCCESSFULLY, response.getMessage());

        verify(customizationRepository).save(mockEntity);

    }

    @Test
    void uploadCustomizationAttributeImages_Success_When_Attribute_And_Files_Are_Null() throws Exception {
        String pantType = "formal";
        String attributeType = "attribute";
        String bucketName = "test-bucket";

        Field bucketNameField = CustomizationServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(customizationServiceImplementation, bucketName);

        CustomizationEntity mockEntity = getCustomizationEntityAA();
        when(customizationRepository.findByPantType(pantType)).thenReturn(Optional.of(mockEntity));


        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        List<AttributeTypeRequest> request = getAttributeTypeRequest();
        request.getFirst().setImages(null);
        request.getFirst().setAttributeId("2");

        S3Utilities mockS3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(mockS3Utilities);

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/formal/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/formal/images/image2.jpg";

        when(mockS3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL(imageUrl1))
                .thenReturn(new URL(imageUrl2));

        SuccessResponse response = customizationServiceImplementation.uploadCustomizationAttributeImages(pantType, attributeType, request);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(Constants.IMAGES_ADDED_SUCCESSFULLY, response.getMessage());

        verify(customizationRepository).save(mockEntity);

    }

    @Test
    void uploadCustomizationAttributeImages_Exception_Customization_Not_Found() throws Exception {
        String pantType = "formal";
        String attributeType = "attribute";

        when(customizationRepository.findByPantType(pantType)).thenReturn(Optional.empty());

        MultipartFile image1 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image1.getInputStream()).thenReturn(new ByteArrayInputStream("dummy-image-content-1".getBytes()));

        MultipartFile[] images = {image1};

        AttributeTypeRequest attributeRequest = new AttributeTypeRequest();
        attributeRequest.setAttributeId("front view");
        attributeRequest.setImages(images);

        List<AttributeTypeRequest> request = Collections.singletonList(attributeRequest);

        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, () ->
                customizationServiceImplementation.uploadCustomizationAttributeImages(pantType, attributeType, request));

        Assertions.assertEquals(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType, exception.getMessage());
    }


    @Test
    void uploadCustomizationAttributeImages_Exception_Convert_From_Json_Error() throws Exception {
        String pantType = "formal";
        String attributeType = "attribute";
        String bucketName = "test-bucket";

        Field bucketNameField = CustomizationServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(customizationServiceImplementation, bucketName);
        CustomizationEntity customizationEntity=getCustomizationEntityAA();
        ObjectMapper mapper=new ObjectMapper();
        customizationEntity.setFitType(mapper.readTree("[{\"Id\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        Optional<CustomizationEntity> entity=Optional.of(customizationEntity);
        when(customizationRepository.findByPantType(pantType)).thenReturn(entity);

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        MultipartFile[] images = {image1, image2};
        List<AttributeTypeRequest> request = getAttributeTypeRequest();
        request.getFirst().setAttributeId("attribute1");
        request.getFirst().setImages(images);

        S3Utilities mockS3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(mockS3Utilities);

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/formal/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/formal/images/image2.jpg";

        when(mockS3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL(imageUrl1))
                .thenReturn(new URL(imageUrl2));

        assertThrows(ConvertFromJsonException.class, ()->customizationServiceImplementation.uploadCustomizationAttributeImages(pantType, attributeType, request));
    }

    @Test
    void uploadCustomizationAttributeImages_Exception_CustomizationType_Not_Found() throws Exception {
        String pantType = "formal";
        String attributeType = "attribute";
        String bucketName = "test-bucket";

        Field bucketNameField = CustomizationServiceImplementation.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(customizationServiceImplementation, bucketName);
        when(customizationRepository.findByPantType(pantType)).thenReturn(Optional.empty());

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile image2 = mock(MultipartFile.class);
        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image2.getOriginalFilename()).thenReturn("image2.jpg");

        InputStream imageInputStream1 = new ByteArrayInputStream("dummy-image-content-1".getBytes());
        InputStream imageInputStream2 = new ByteArrayInputStream("dummy-image-content-2".getBytes());
        when(image1.getInputStream()).thenReturn(imageInputStream1);
        when(image2.getInputStream()).thenReturn(imageInputStream2);

        MultipartFile[] images = {image1, image2};
        List<AttributeTypeRequest> request = getAttributeTypeRequest();
        request.getFirst().setAttributeId("attribute1");
        request.getFirst().setImages(images);

        S3Utilities mockS3Utilities = mock(S3Utilities.class);
        when(s3Client.utilities()).thenReturn(mockS3Utilities);

        String imageUrl1 = "https://s3.amazonaws.com/test-bucket/formal/images/image1.jpg";
        String imageUrl2 = "https://s3.amazonaws.com/test-bucket/formal/images/image2.jpg";

        when(mockS3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL(imageUrl1))
                .thenReturn(new URL(imageUrl2));

        CustomizationNotFoundException exception = assertThrows(CustomizationNotFoundException.class, ()->customizationServiceImplementation.uploadCustomizationAttributeImages(pantType, attributeType, request));
        Assertions.assertEquals(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType,exception.getMessage());
    }

    private List<AttributeTypeRequest> getAttributeTypeRequest() {
        List<AttributeTypeRequest> list = new ArrayList<>();
        AttributeTypeRequest attributeTypeRequest = new AttributeTypeRequest();
        attributeTypeRequest.setAttributeId("1");
        list.add(attributeTypeRequest);
        return list;
    }

    private UpdateFabricRequestList getUpdateFabricRequestList() {
        UpdateFabricRequestList list = new UpdateFabricRequestList();
        list.setCustomizationId("custom1");
        list.setFabricsRequestList(getUpdatedListFabricRequest());
        return list;
    }

    private AddFabricRequest getAddFabricRequest() {
        AddFabricRequest fabricRequest=new AddFabricRequest();
        fabricRequest.setCustomizationId("custom1");
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


    private CustomizationEntity getCustomizationEntity() throws Exception {
        CustomizationEntity customizationEntity=new CustomizationEntity();
        customizationEntity.setCustomizationId("Customization123");
        customizationEntity.setPantType("formal");
        customizationEntity.setFabric(getFabricRequest());
        customizationEntity.setFlyType(getFlyType());
        customizationEntity.setFitType(getFitType());
        customizationEntity.setPocketType(getPocketType());
        customizationEntity.setButtonType(getButtonType());
        customizationEntity.setCustomizationImage(getCustomizationImage());
        customizationEntity.setTrueWaistMeasurement(getTruewaistMeasurement());
        customizationEntity.setPantPleatsType(getPantPleatsTypes());
        customizationEntity.setPantCuffsType(getPantCuffsTypes());
        customizationEntity.setPantInSeamLength(getPantInSeamLength());
        customizationEntity.setPantOutSeamLength(getPantOutSeamLength());
        customizationEntity.setRiseType(getRiseTypes());
        customizationEntity.setButtonTypes(getButtonTypes());
        customizationEntity.setPocketTypes(getPocketTypes());
        customizationEntity.setTrueWaistMeasurements(List.of(22,23,24,25,26,27,28,29,30));
        customizationEntity.setPantInSeamLengths(List.of(22,23,24,25,26,27,28,29,30));
        customizationEntity.setPantOutSeamLengths(List.of(22,23,24,25,26,27,28,29,30));
        customizationEntity.setFitTypes(getListFitType());
        customizationEntity.setRiseTypes(getRiseType());
        customizationEntity.setFlyTypes(getListFlyType());
        customizationEntity.setPantCuffsTypes(getPantCuffsType());
        customizationEntity.setPantPleatsTypes(getPantPleatsType());
        customizationEntity.setFabrics(getFabrics());
        customizationEntity.setCustomizationImages(getCustomizationImages());
        return customizationEntity;
    }

    private JsonNode getCustomizationImage() throws Exception{
        String flyTypeJson = "{ \"attributeType\": \"Front View\", \"attributeImagesUrl\":\"https://product-images-2024.s3.ap-south-1.amazonaws.com/customizationImages/images/front-view.svg\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getButtonType() throws Exception{
        String flyTypeJson = "{ \"attributeType\": \"Regular\", \"attributeImagesUrl\":\"https://product-images-2024.s3.ap-south-1.amazonaws.com/buttons/images/regular.svg\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getPocketType() throws Exception{
        String flyTypeJson = "{ \"attributeType\": \"Straight\", \"attributeImagesUrl\":\"https://product-images-2024.s3.ap-south-1.amazonaws.com/pantCuffsType/images/pantcuffs.png\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getRiseTypes() throws Exception {
        String flyTypeJson = "{ \"attributeType\": \"High rise\", \"attributeImagesUrl\":\"https://product-images-2024.s3.ap-south-1.amazonaws.com/riseType/images/high.jpeg\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getTruewaistMeasurement() throws  Exception{
        String flyTypeJson = "{ \"trueWaistMeasurements\": \"[22,23,24,25,26,27,28,29,30] \" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getPantPleatsTypes() throws Exception {
        String flyTypeJson = "{ \"attributeType\": \"Single Pleat\", \"attributeImagesUrl\":\"https://product-images-2024.s3.ap-south-1.amazonaws.com/pantPleat/images/1pleat.png\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getPantOutSeamLength() throws Exception {
        String flyTypeJson = "{ \"pantInSeamLengths\": \"[22,23,24,25,26,27,28,29,30] \" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getPantInSeamLength() throws Exception {
        String flyTypeJson = "{ \"pantInSeamLength\": \"[22,23,24,25,26,27,28,29,30] \" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getPantCuffsTypes() throws Exception {
        String flyTypeJson = "{ \"attributeType\": \"Regular\", \"attributeImagesUrl\": \"https://product-images-2024.s3.ap-south-1.amazonaws.com/fitType/images/realxed.jpeg\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getFitType() throws Exception {
        String flyTypeJson = "{ \"attributeType\": \"slim\", \"attributeImagesUrl\": \"https://product-images-2024.s3.ap-south-1.amazonaws.com/fitType/images/realxed.jpeg\" }";
        return objectMapper.readTree(flyTypeJson);
    }


    private JsonNode getFlyType() throws Exception {
        String flyTypeJson = "{ \"attributeType\": \"zipper\", \"attributeImagesUrl\": \"https://product-images-2024.s3.ap-south-1.amazonaws.com/flyTypes/images/fly.svg\" }";
        return objectMapper.readTree(flyTypeJson);
    }

    private JsonNode getFabricRequest() throws Exception{
        String fabricDetailsJson = "{ \"Color_code\": \"#000000\", \"Description\": \"cotton\", \"color\": \"blue\" }";
        return objectMapper.readTree(fabricDetailsJson);
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

    private List<Fabric> getFabrics(){
        List<Fabric> list=new ArrayList<>();
        Fabric fabric=new Fabric();
        fabric.setFabricId("FabricId1");
        fabric.setFabricColorCode("#000000");
        fabric.setFabricDescription("cotton");
        fabric.setImageUrl(List.of("FabricImg1","FabricImg2"));
        fabric.setFabricColor("white");
        fabric.setFabricPrice(100.0);
        fabric.setProductOfferPercentage(5.0);
        list.add(fabric);
        return list;
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
        customizationAttribute.setAttributeImagesUrl(List.of("pantPleatsTypeValue1","pantPleatsTypeValue2"));
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
        fabricRequest.setImageUrl(List.of("www.images.com"));
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


    private CustomizationEntity getCustomizationEntityAA() throws Exception{
        CustomizationEntity customization = new CustomizationEntity();
        customization.setCustomizationId("CUST001");
        customization.setPantType("Slim Fit");

        ObjectMapper mapper = new ObjectMapper();

        customization.setTrueWaistMeasurement(mapper.readTree("[30, 32, 34]"));
        customization.setPantInSeamLength(mapper.readTree("[30, 32, 34]"));
        customization.setPantOutSeamLength(mapper.readTree("[40, 42, 44]"));

        customization.setFitType(mapper.readTree("[{\"attributeId\":1, \"attributeType\": \"Regular\"}, {\"attributeId\": 2, \"attributeType\": \"Slim\"}]"));
        customization.setRiseType(mapper.readTree("[{\"attributeId\": 1, \"attributeType\": \"Low Rise\"}, {\"attributeId\": 2, \"attributeType\": \"Mid Rise\"}]"));

        customization.setPocketType(mapper.readTree("{\"sidePocketTypes\":[{\"attributeId\": \"Side Pockets\", \"attributeType\":\"backPocketType\"}]}"));
        customization.setButtonType(mapper.readTree("{\"frontButtonTypes\":[{\"attributeId\": 1, \"attributeType\": \"Black\"}, {\"attributeId\": 2, \"attributeType\": \"Brown\"}]}"));

        customization.setFlyType(mapper.readTree("[{\"attributeId\": 1, \"attributeType\": \"Zipper\"}, {\"attributeId\": 2, \"attributeType\": \"Button Fly\"}]"));
        customization.setFabric(mapper.readTree("[{\"fabricId\": 1, \"fabricDescription\": \"Denim\"}, {\"fabricId\": 2, \"fabricDescription\": \"Cotton\"}]"));

        customization.setPantCuffsType(mapper.readTree("[{\"attributeId\": 1, \"attributeType\": \"Cuffed\"}, {\"attributeId\": 2, \"attributeType\": \"Uncuffed\"}]"));
        customization.setPantPleatsType(mapper.readTree("[{\"attributeId\": 1, \"attributeType\": \"Single Pleat\"}, {\"attributeId\": 2, \"attributeType\": \"Double Pleat\"}]"));
        customization.setCustomizationImage(mapper.readTree("[{\"attributeId\": \"front View\", \"attributeType\": \"Front View\", \"attributeImagesUrl\": [\"https://product-images-2024.s3.ap-south-1.amazonaws.com/customizationImages/images/front-view.svg\"]}, {\"attributeId\": \"back View\", \"attributeType\": \"Back View\", \"attributeImagesUrl\": [\"https://product-images-2024.s3.ap-south-1.amazonaws.com/customizationImages/images/back-view.svg\"]}]"));

        customization.setTrueWaistMeasurements(List.of(30, 32, 34));
        customization.setPantInSeamLengths(List.of(30, 32, 34));
        customization.setPantOutSeamLengths(List.of(40, 42, 44));

        customization.setFitTypes(List.of(
                new CustomizationAttribute("1", "Relaxed",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/fitType/images/realxed.jpeg")),
                new CustomizationAttribute("2", "Slim",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/fitType/images/slim-fit.jpeg"))
        ));
        customization.setRiseTypes(List.of(
                new CustomizationAttribute("1", "Mid Rise",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/riseType/images/mid.jpeg")),
                new CustomizationAttribute("2", "Low Rise",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/riseType/images/low.jpeg"))
        ));

        customization.setPocketTypes(getPocketTypes());
        customization.setButtonTypes(getButtonTypes());
        customization.setFlyTypes(List.of(
                new CustomizationAttribute("1", "Zipper",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/flyTypes/images/fly.svg")),
                new CustomizationAttribute("2", "Button",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/flyTypes/images/buttonfly.svg"))
        ));
        customization.setPantCuffsTypes(List.of(
                new CustomizationAttribute("1", "Regular",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/pantCuffsType/images/withoutcuffs.png")),
                new CustomizationAttribute("2", "Folded",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/pantCuffsType/images/pantcuffs.png"))
        ));
        customization.setPantPleatsTypes(List.of(
                new CustomizationAttribute("1", "Single Pleat",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/pantPleat/images/1pleat.png")),
                new CustomizationAttribute("2", "Double Pleat",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/pantPleat/images/2%20pleats.svg"))
        ));
        customization.setFabrics(getFabrics());
        customization.setCustomizationImages(List.of(
                new CustomizationAttribute("front View", "Front View",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/customizationImages/images/front-view.svg")),
                new CustomizationAttribute("back View", "Back View",List.of("https://product-images-2024.s3.ap-south-1.amazonaws.com/customizationImages/images/back-view.svg"))
        ));
return customization;
    }
}