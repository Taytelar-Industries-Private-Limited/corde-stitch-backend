package com.cordestitch.controller.customization;

import com.cordestitch.request.customization.admincustomization.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.customization.CustomizationResponse;
import com.cordestitch.service.service.customization.CustomizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customization")
@RequiredArgsConstructor
public class CustomizationController {

    private final CustomizationService customizationService;

    /**
     * This API endpoint is used to add customization data for a specific product.
     * The customization data includes various parameters like measurements, fabric details,
     * and types of fits. The data is sent in the request body in the form of a CustomizationRequest object.
     *
     * @param request A valid CustomizationRequest object containing the customization data.
     * @return ResponseEntity<SuccessResponse> - A response containing a success message and status.
     * HTTP Status Codes:
     * 200 OK - When the data is successfully added.
     */
    @PostMapping("/addCustomizationData")
    public ResponseEntity<SuccessResponse>  addData(@Valid @RequestBody CustomizationRequest request) {
        SuccessResponse response = customizationService.addData(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * This API endpoint is used to retrieve the customization options for a specific pant type.
     * The pant type is provided as a query parameter (e.g., pantType=jeans), and the response includes
     * customization details like waist measurements, fabric options, fit types, and other attributes.
     *
     * @param pantType A String representing the pant type for which customization options are requested.
     * @return ResponseEntity<CustomizationResponse> - A response containing the customization data
     *         for the requested pant type.
     * HTTP Status Codes:
     * 200 OK - When the data is successfully retrieved.
     */
    @GetMapping("/getCustomizationType")
    public ResponseEntity<CustomizationResponse> getCustomizationType(@Valid @RequestParam String pantType) {
        CustomizationResponse response = customizationService.getCustomizationType(pantType);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * This API endpoint is used to update customization data for a specific pant type.
     * The update request contains various customization attributes that need to be modified
     * in the system. This includes details such as waist measurements, fabric choices,
     * and fit types. The endpoint processes the request and returns a success response
     * if the update is successful.
     *
     * @param request An UpdateCustomizationRequest object containing the customization
     *                details to be updated.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of
     *         the update operation.
     * HTTP Status Codes:
     * 200 OK - When the customization data is successfully updated.
     */
    @PutMapping("/updateCustomizationData")
    public ResponseEntity<SuccessResponse> updateCustomizationData(@Valid @RequestBody UpdateCustomizationRequest request){
        SuccessResponse response = customizationService.updateCustomizationData(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * This API endpoint is used to add new fabric data to the system.
     * The request contains various fabric attributes that need to be registered,
     * including details such as fabric type, color, description, and price.
     * The endpoint processes the request and returns a success response if the
     * fabric data is successfully added.
     *
     * @param request An AddFabricRequest object containing the fabric details
     *                to be added.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of
     *         the add operation.
     * HTTP Status Codes:
     * 200 OK - When the fabric data is successfully added.
     */
    @PostMapping("/addFabricData")
    public ResponseEntity<SuccessResponse> addFabricData(@Valid @RequestBody AddFabricRequest request){
        SuccessResponse response = customizationService.addFabricData(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * This API endpoint is used to update existing fabric data in the system.
     * The update request contains a list of fabric attributes that need to be modified,
     * including details such as fabric type, color, description, and price.
     * The endpoint processes the request and returns a success response if the
     * fabric data is successfully updated.
     *
     * @param request An UpdateFabricRequestList object containing the fabric details
     *                to be updated.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of
     *         the update operation.
     * HTTP Status Codes:
     * 200 OK - When the fabric data is successfully updated.
     */
    @PutMapping("/updateFabricData")
    public ResponseEntity<SuccessResponse> updateFabricData(@Valid @RequestBody UpdateFabricRequestList request){
        SuccessResponse response = customizationService.updateFabricData(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * This API endpoint is used to delete fabric data from the system based on the specified
     * customization ID and fabric ID. The endpoint processes the request and returns a success
     * response if the fabric data is successfully deleted.
     *
     * @param customizationId The ID of the customization associated with the fabric data to be deleted.
     * @param fabricId The ID of the fabric to be deleted.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of the
     *         delete operation.
     * HTTP Status Codes:
     * 200 OK - When the fabric data is successfully deleted.
     */
    @DeleteMapping("/deleteFabricData")
    public ResponseEntity<SuccessResponse> deleteFabricData(@RequestParam String customizationId, @RequestParam String fabricId){
        SuccessResponse response = customizationService.deleteFabricData(customizationId, fabricId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * This API endpoint is used to delete customization data from the system based on the specified
     * customization ID. The endpoint processes the request and returns a success
     * response if the fabric data is successfully deleted.
     *
     * @param customizationId The ID of the customization associated with the data to be deleted.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of the
     *         delete operation.
     * HTTP Status Codes:
     * 200 OK - When the customization data is successfully deleted.
     */
    @DeleteMapping("/deleteCustomizationData")
    public ResponseEntity<SuccessResponse>  deleteCustomizationData(@RequestParam String customizationId) {
        SuccessResponse response = customizationService.deleteCustomizationData(customizationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * This API endpoint is used to upload fabric files, including images and a video,
     * associated with a specified pant type and fabric ID. The endpoint processes
     * the request and returns a success response if the fabric files are successfully
     * uploaded to the system.
     *
     * @param pantType The type of pants to which the fabric files are associated.
     * @param fabricId The ID of the fabric for which the files are being uploaded.
     * @param images An array of MultipartFile objects representing the images of the fabric.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of
     *         the upload operation.
     * HTTP Status Codes:
     * 200 OK - When the fabric files are successfully uploaded.
     */
    @PostMapping("/uploadFabricFiles")
    public ResponseEntity<SuccessResponse> uploadFabricFiles(
            @RequestParam("pantType") String pantType,
            @RequestParam("fabricId") String fabricId,
            @RequestParam("images") MultipartFile[] images) {

        SuccessResponse response = customizationService.uploadFabricFiles(pantType,fabricId, images);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * This API endpoint is used to upload customization images associated with a specified
     * pant type. The endpoint processes the request and returns a success response if
     * the customization images are successfully uploaded to the system.
     *
     * @param pantType The type of pants for which the customization images are being uploaded.
     * @return ResponseEntity<SuccessResponse> - A response indicating the outcome of
     *         the upload operation.
     * HTTP Status Codes:
     * 200 OK - When the customization images are successfully uploaded.
     */

    @PostMapping("/uploadCustomizationAttributeImages")
    public ResponseEntity<SuccessResponse> uploadImages(
            @RequestParam String pantType,
            @RequestParam String attributeType,
            @ModelAttribute AttributeTypeRequestList requestList) {

        SuccessResponse response = customizationService.uploadCustomizationAttributeImages(pantType,attributeType, requestList.getAttributeRequests());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
