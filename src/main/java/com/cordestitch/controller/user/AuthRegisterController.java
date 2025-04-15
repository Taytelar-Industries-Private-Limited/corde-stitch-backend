package com.cordestitch.controller.user;

import com.cordestitch.request.user.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.otp.UpdateDataResponse;
import com.cordestitch.response.user.ServiceResponse;
import com.cordestitch.response.user.UserDetailsResponse;
import com.cordestitch.service.service.user.AuthRegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRegisterController {

    private final AuthRegisterService authRegisterService;

    /**
     * Updates the user's email address or phone number.
     * This API endpoint allows users to update their contact information, such as email or phone number.
     * It takes an UpdateRequest object containing the new email or phone number details.
     * If the update is successful, it returns a SuccessResponse confirming the update.
     *
     * @param request The request object containing the updated email or phone number details.
     * @return A ResponseEntity containing a SuccessResponse confirming the update.
     */
    @PostMapping("/updateEmailAddressOrPhoneNumber")
    public ResponseEntity<SuccessResponse> updateEmailAddressOrPhoneNumber(@Valid @RequestBody UpdateRequest request) {
        SuccessResponse response = authRegisterService.updateEmailAddressOrPhoneNumber(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Validates the OTP for email or phone number verification.
     * This API endpoint verifies the one-time password (OTP) sent to the user's email or phone number.
     * It takes a ValidateRequest object containing the OTP details.
     * Upon successful validation, it returns a SuccessResponse indicating the OTP was validated.
     *
     * @param request The request object containing OTP details for verification.
     * @return A ResponseEntity containing a SuccessResponse confirming OTP validation.
     */
    @PostMapping("/validateOTPForEmailOrPhoneNumber")
    public ResponseEntity<SuccessResponse> validateOTPForEmailOrPhoneNumber(@Valid @RequestBody ValidateRequest request) {
        SuccessResponse response = authRegisterService.validateOTPForEmailOrPhoneNumber(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Checks if the user's email or phone number is verified.
     * This API endpoint retrieves the verification status of the user’s contact details (email or phone number).
     * It takes a userId as a query parameter to identify the user.
     * The response includes an UpdateDataResponse indicating whether the email or phone number is verified.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing an UpdateDataResponse with the verification status.
     */
    @GetMapping("/checkEmailOrPhoneNumberVerified")
    public ResponseEntity<UpdateDataResponse> checkEmailOrPhoneNumberVerified(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        UpdateDataResponse response = authRegisterService.checkEmailOrPhoneNumberVerified(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates the user's profile information.
     * This API endpoint allows users to update their profile details, such as name, address, or other personal information.
     * It accepts an UpdateProfileRequest object containing the updated profile details.
     * If the update is successful, it returns a SuccessResponse indicating that the profile was updated.
     *
     * @param request The request object containing the updated profile details.
     * @return A ResponseEntity containing a SuccessResponse confirming the profile update.
     */
    @PutMapping("/updateProfile")
    public ResponseEntity<SuccessResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        SuccessResponse response = authRegisterService.updateProfile(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * API to get user details based on the user ID present in the request attributes.
     *
     * @param request the HTTP request containing the "userId" attribute, which represents the unique ID of the user.
     * @return a ResponseEntity containing the user details in the response body with an HTTP status of 200 (OK).
     *
     * @apiNote This endpoint retrieves detailed information about the user, such as personal and account-related details.
     */
    @GetMapping("/getUserDetails")
    public ResponseEntity<UserDetailsResponse> getUserDetails(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        UserDetailsResponse response = authRegisterService.getUserDetails(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * API to update user details based on the data provided in the request body.
     *
     * @param request the request body containing the updated user details, including fields like first name,
     *                last name, gender, email address, and optionally address details.
     * @return a ResponseEntity containing a success response message and an HTTP status of 200 (OK).
     *
     * @apiNote This endpoint updates the user information stored in the system, including both personal and
     *          address-related details. Validation is applied to ensure that the input data meets required criteria.
     */
    @PutMapping("/updateUserDetails")
    public ResponseEntity<SuccessResponse> updateUserDetails(@Valid @RequestBody UpdateUserDetailsRequest request) {
        SuccessResponse response = authRegisterService.updateUserDetails(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/check-service-availability")
    public ResponseEntity<ServiceResponse> checkServiceAvailability(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse serviceResponse = authRegisterService.checkServiceAvailability(request);
        return ResponseEntity.status(HttpStatus.OK).body(serviceResponse);
    }
}