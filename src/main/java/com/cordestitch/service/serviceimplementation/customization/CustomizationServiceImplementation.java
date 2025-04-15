package com.cordestitch.service.serviceimplementation.customization;

import com.cordestitch.entity.customization.*;
import com.cordestitch.exception.customization.*;
import com.cordestitch.exception.product.S3UploadException;
import com.cordestitch.repository.customization.CustomizationRepository;
import com.cordestitch.request.customization.admincustomization.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizationResponse;
import com.cordestitch.service.service.customization.CustomizationService;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Slf4j
public class CustomizationServiceImplementation implements CustomizationService {

    private final CustomizationRepository customizationRepository;

    private final Generator generator;

    private final S3Client s3Client;

    private final ModelMapper modelMapper;

    @Value("${aws.buckets.productImages}")
    private String bucketName;

    @Transactional
    @Override
    public SuccessResponse addData(CustomizationRequest request) {
        log.info("Customization request: {}", request);
        Optional<CustomizationEntity> entity = customizationRepository.findByPantType(request.getPantType());
        if (entity.isPresent()) {
            throw new CustomizationTypeAlreadyExistException(Constants.CUSTOMIZATION_TYPE_ERROR);
        }

        CustomizationEntity customizationEntity = new CustomizationEntity();

        customizationEntity.setCustomizationId(generator.generateId(Constants.CUSTOMIZATION_ID));

        customizationEntity.setPantType(request.getPantType());

        customizationEntity.setTrueWaistMeasurements(request.getTrueWaistMeasurement());
        customizationEntity.setPantInSeamLengths(request.getPantInSeamLength());
        customizationEntity.setPantOutSeamLengths(request.getPantOutSeamLength());
        customizationEntity.setFitTypes(setToCustomizationFields(request.getFitType()));
        customizationEntity.setRiseTypes(setToCustomizationFields(request.getRiseType()));
        customizationEntity.setFlyTypes(setToCustomizationFields(request.getFlyType()));
        customizationEntity.setPantCuffsTypes(setToCustomizationFields(request.getPantCuffsType()));
        customizationEntity.setPantPleatsTypes(setToCustomizationFields(request.getPantPleatsType()));
        customizationEntity.setFabrics(setToFabrics(request.getFabric()));
        customizationEntity.setPocketTypes(setToPockets(request.getPocketTypes()));
        customizationEntity.setButtonTypes(setToButtons(request.getButtonTypes()));
        customizationEntity.setCustomizationImages(setToCustomizationFields(request.getCustomizationImages()));

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);
        log.info("Customization Entity : {}", customizationEntity);

        return new SuccessResponse(Constants.CUSTOMIZATION_DATA_ADDED_SUCCESSFULLY + request.getPantType(), HttpStatus.OK.value());
    }



    @Override
    @Cacheable(value = "customizationTypeCache", key = "'getCustomizationType'")
    public CustomizationResponse getCustomizationType(String pantType) {

        log.info("Get Customization Type request: {}", pantType);
        log.info("Cache miss getCustomizationType fetched from db {} ", LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        Optional<CustomizationEntity> entity = customizationRepository.findByPantType(pantType);
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType);
        }
        CustomizationEntity customizationEntity = entity.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        return modelMapper.map(customizationEntity, CustomizationResponse.class);
    }

    @Transactional
    @Override
    public SuccessResponse updateCustomizationData(UpdateCustomizationRequest request) {
        log.info("Update Customization Type request: {}", request);

        Optional<CustomizationEntity> entity = customizationRepository.findByCustomizationId(request.getCustomizationId());
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + request.getCustomizationId());
        }

        CustomizationEntity customizationEntity = entity.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        customizationEntity.setPantType(request.getPantType());
        customizationEntity.setTrueWaistMeasurements(request.getTrueWaistMeasurement());
        customizationEntity.setPantInSeamLengths(request.getPantInSeamLength());
        customizationEntity.setPantOutSeamLengths(request.getPantOutSeamLength());
        customizationEntity.setFitTypes(request.getFitType());
        customizationEntity.setRiseTypes(request.getRiseType());
        customizationEntity.setPocketTypes(request.getPocketTypes());
        customizationEntity.setButtonTypes(request.getButtonTypes());
        customizationEntity.setFlyTypes(request.getFlyType());
        customizationEntity.setPantPleatsTypes(request.getPantPleatsType());
        customizationEntity.setCustomizationImages(request.getCustomizationImages());
        customizationEntity.setPantCuffsTypes(request.getPantCuffType());
        customizationEntity.setFabrics(updateToFabrics(request.getFabric(), customizationEntity.getFabrics()));

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);
        log.info("Updated Customization Entity: {}", customizationEntity);

        return new SuccessResponse(Constants.CUSTOMIZATION_DATA_UPDATED_SUCCESSFULLY + request.getPantType(), HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse addFabricData(AddFabricRequest request) {

        log.info("Add Fabric Data request: {}", request);
        Optional<CustomizationEntity> entity = customizationRepository.findByCustomizationId(request.getCustomizationId());
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + request.getCustomizationId());
        }

        CustomizationEntity customizationEntity = entity.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        List<Fabric> fabricEntities = setToFabrics(request.getFabricRequestList());
        customizationEntity.getFabrics().addAll(fabricEntities);

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);
        log.info("Update after adding fabric data : {}", customizationEntity);

        return new SuccessResponse(Constants.FABRIC_ADDED_SUCCESSFULLY + customizationEntity.getPantType(), HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse deleteFabricData(String customizationId, String fabricId) {

        log.info("Delete Fabric Data request: customizationId = {}, fabricId = {}", customizationId, fabricId);
        Optional<CustomizationEntity> entity = customizationRepository.findByCustomizationId(customizationId);
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + customizationId);
        }

        CustomizationEntity customizationEntity = entity.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        boolean fabricRemoved = customizationEntity.getFabrics().removeIf(fabric -> fabric.getFabricId().equals(fabricId));

        if (!fabricRemoved) {
            throw new FabricNotFoundException(Constants.FABRIC_NOT_FOUND_ERROR + fabricId);
        }

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);
        log.info("Update after deleting fabric data: {}", customizationEntity);

        return new SuccessResponse(Constants.FABRIC_DELETED_SUCCESSFULLY + customizationEntity.getPantType(), HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse deleteCustomizationData(String customizationId) {

        log.info("Delete Customization Data request: customizationId = {}", customizationId);
        Optional<CustomizationEntity> entity = customizationRepository.findById(customizationId);
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + customizationId);
        }
        customizationRepository.deleteById(customizationId);

        return new SuccessResponse(Constants.CUSTOMIZATION_DATA_DELETED,HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse updateFabricData(UpdateFabricRequestList request) {

        log.info("Update Fabric Data List request: {}", request);
        Optional<CustomizationEntity> entity = customizationRepository.findByCustomizationId(request.getCustomizationId());
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + request.getCustomizationId());
        }

        CustomizationEntity customizationEntity = entity.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        List<Fabric> updatedFabrics = updateToFabrics(request.getFabricsRequestList(), customizationEntity.getFabrics());
        customizationEntity.setFabrics(updatedFabrics);

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);
        log.info("Updated fabric data list: {}", customizationEntity);

        return new SuccessResponse(Constants.FABRIC_UPDATED_SUCCESSFULLY + customizationEntity.getPantType(), HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse uploadFabricFiles(String pantType, String fabricId, MultipartFile[] images) {

        Optional<CustomizationEntity> entity = customizationRepository.findByPantType(pantType);
        if (entity.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType);
        }

        CustomizationEntity customizationEntity = entity.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        List<Fabric> fabrics = customizationEntity.getFabrics();
        Fabric fabric = fabrics.stream()
                .filter(fabricData -> fabricData.getFabricId().equals(fabricId))
                .findFirst()
                .orElseThrow(() -> new FabricNotFoundException(Constants.FABRIC_NOT_FOUND_ERROR + fabricId));

        List<String> imageUrls = processFilesToS3(images, fabricId);

        fabric.setImageUrl(imageUrls);

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);

        return new SuccessResponse(Constants.IMAGES_AND_VIDEO_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    @Transactional
    public SuccessResponse uploadCustomizationAttributeImages(String pantType,String attributeType, List<AttributeTypeRequest> request) {
        Optional<CustomizationEntity> customizationEntityOptional = customizationRepository.findByPantType(pantType);
        if (customizationEntityOptional.isEmpty()) {
            throw new CustomizationNotFoundException(Constants.CUSTOMIZATION_TYPE_NOT_FOUND + pantType);
        }

        CustomizationEntity customizationEntity = customizationEntityOptional.get();

        try {
            customizationEntity.convertFromJSON();
        } catch (IOException e) {
            throw new ConvertFromJsonException(Constants.CONVERT_FROM_JSON_ERROR + e);
        }

        for (AttributeTypeRequest attributeTypeRequest : request) {
            String attributeId = attributeTypeRequest.getAttributeId();
            MultipartFile[] files = attributeTypeRequest.getImages();

            CustomizationAttribute attribute = findAttributeInCustomizationEntity(customizationEntity, attributeId);

            if (attribute != null && files != null) {
                List<String> uploadedUrls = processFilesToS3(files, attributeType);
                attribute.getAttributeImagesUrl().addAll(uploadedUrls);
            }
        }

        try {
            customizationEntity.convertToJSON();
        } catch (Exception e) {
            throw new ConvertToJsonException(Constants.CONVERT_TO_JSON_EXCEPTION + e);
        }

        customizationRepository.save(customizationEntity);
        log.info("Updated Customization Entity with images: {}", customizationEntity);

        return new SuccessResponse(Constants.IMAGES_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    private List<String> processFilesToS3(MultipartFile[] imageFiles, String type) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : imageFiles) {
            String fileUrl = uploadFileToS3(file, type);
            fileUrls.add(fileUrl);
        }
        log.info("Images URLs: {}", fileUrls);
        return fileUrls;
    }


    private String uploadFileToS3(MultipartFile file, String pantType) {
        String slash = "/";
        String fileName = pantType + slash + "images" + slash + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build(),
                    RequestBody.fromInputStream(inputStream, file.getSize()));

            return s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build()).toExternalForm();
        } catch (IOException e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new S3UploadException(Constants.UPLOAD_ERROR);
        }
    }

    private List<Fabric> setToFabrics(List<FabricRequest> fabricRequests) {
        log.info("Fabric Request: {}", fabricRequests);

        List<Fabric> fabrics = fabricRequests.stream()
                .map(request -> {
                    Fabric saveFabric = new Fabric();
                    saveFabric.setFabricId(generator.generateId(Constants.FABRIC_ID));
                    saveFabric.setFabricColor(request.getFabricColor());
                    saveFabric.setFabricColorCode(request.getFabricColorCode());
                    saveFabric.setFabricDescription(request.getFabricDescription());
                    saveFabric.setFabricPrice(request.getFabricPrice());
                    saveFabric.setProductOfferPercentage(request.getProductOfferPercentage());
                    if(!(request.getImageUrl().isEmpty())){
                        saveFabric.setImageUrl(request.getImageUrl());
                    }
                    return saveFabric;
                })
                .toList();

        log.info("Fabric List: {}", fabrics);
        return fabrics;
    }

    private List<CustomizationAttribute> setToCustomizationFields(List<CustomizationAttribute> customizationAttributes) {
        log.info("Customization Request: {}", customizationAttributes);

        List<CustomizationAttribute> processedFields = customizationAttributes.stream()
                .map(request -> {
                    CustomizationAttribute saveCustomizationAttribute = new CustomizationAttribute();
                    saveCustomizationAttribute.setAttributeId(request.getAttributeId());
                    saveCustomizationAttribute.setAttributeType(request.getAttributeType());
                    if (!(request.getAttributeImagesUrl().isEmpty())) {
                        saveCustomizationAttribute.setAttributeImagesUrl(request.getAttributeImagesUrl());
                        return saveCustomizationAttribute;
                    }
                    return saveCustomizationAttribute;
                })
                .toList();

        log.info("Customization fields: {}", processedFields);
        return processedFields;
    }

    private List<Fabric> updateToFabrics(List<UpdateFabricRequest> fabricRequest, List<Fabric> fabricsList) {
        log.info("Fabric Request List: {}", fabricRequest);
        log.info("Fabric List: {}", fabricsList);

        Map<String, UpdateFabricRequest> requestMap = fabricRequest.stream()
                .collect(Collectors.toMap(UpdateFabricRequest::getFabricId, request -> request));

        return fabricsList.stream()
                .map(fabric -> {
                    UpdateFabricRequest request = requestMap.get(fabric.getFabricId());
                    if (request != null) {
                        fabric.setFabricColor(request.getFabricColor());
                        fabric.setFabricColorCode(request.getFabricColorCode());
                        fabric.setFabricPrice(request.getFabricPrice());
                        fabric.setFabricDescription(request.getFabricDescription());
                        fabric.setProductOfferPercentage(request.getProductOfferPercentage());
                    }
                    return fabric;
                })
                .toList();

    }

    private CustomizationAttribute findAttributeInCustomizationEntity(CustomizationEntity entity, String attributeId) {
        List<List<CustomizationAttribute>> allAttributeLists = Arrays.asList(
                entity.getFitTypes(),
                entity.getRiseTypes(),
                entity.getPocketTypes().getBackPocketTypes(),
                entity.getPocketTypes().getSidePocketTypes(),
                entity.getPocketTypes().getPocketTypeImagesUrl(),
                entity.getButtonTypes().getBackButtonTypes(),
                entity.getButtonTypes().getFrontButtonTypes(),
                entity.getButtonTypes().getButtonTypeImagesUrl(),
                entity.getFlyTypes(),
                entity.getPantCuffsTypes(),
                entity.getPantPleatsTypes(),
                entity.getCustomizationImages()
        );

        return allAttributeLists.stream()
                .flatMap(List::stream)
                .filter(attribute -> attribute.getAttributeId().equals(attributeId))
                .findFirst()
                .orElseThrow(() -> new CustomizationNotFoundException(Constants.CUSTOMIZATION_NOT_FOUND_ERROR + attributeId));
    }

    private PocketTypes setToPockets(PocketTypes request) {
        PocketTypes pocketTypes = new PocketTypes();
        pocketTypes.setBackPocketTypes(setToCustomizationFields(request.getBackPocketTypes()));
        pocketTypes.setSidePocketTypes(setToCustomizationFields(request.getSidePocketTypes()));
        pocketTypes.setPocketTypeImagesUrl(setToCustomizationFields(request.getPocketTypeImagesUrl()));
        return pocketTypes;
    }

    private ButtonTypes setToButtons(ButtonTypes request) {
        ButtonTypes buttonTypes = new ButtonTypes();
        buttonTypes.setFrontButtonTypes(setToCustomizationFields(request.getFrontButtonTypes()));
        buttonTypes.setBackButtonTypes(setToCustomizationFields(request.getBackButtonTypes()));
        buttonTypes.setButtonTypeImagesUrl(setToCustomizationFields(request.getButtonTypeImagesUrl()));
        return buttonTypes;
    }

}
