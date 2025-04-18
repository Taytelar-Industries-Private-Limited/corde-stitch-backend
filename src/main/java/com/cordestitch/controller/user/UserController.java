package com.cordestitch.controller.user;

import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.request.user.LoginRequest;
import com.cordestitch.request.user.UserBankDetailsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.response.user.LoginResponse;
import com.cordestitch.response.user.UserBankDetailsResponse;
import com.cordestitch.service.service.user.UserService;
import com.cordestitch.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * Endpoint to authenticate and log in a user.
     *
     * @param loginRequest The request object containing user login details (phone number, user type, and request type).
     * @return A ResponseEntity containing the LoginResponse object and HTTP status 200 (OK) if login is successful.
     *         If the login fails or validation errors occur, appropriate error responses will be returned.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(loginRequest, request, response);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    /**
     * Endpoint to log out a user.
     *
     * @param response The HttpServletResponse object used to clear the JWT cookie from the client's browser.
     * @return A ResponseEntity containing a SuccessResponse object and HTTP status 200 (OK) indicating
     *         that the user has been successfully logged out. The JWT cookie is cleared to prevent further
     *         authentication until a new login is performed.
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletResponse response) {
        SuccessResponse successResponse = userService.logout(response);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }


    /**
     * Endpoint to add a new address for a user.
     *
     * @param addressRequest The request object containing the address details to be added (building name, street name, city, state, pin code, etc.).
     * @return A ResponseEntity containing the SuccessResponse object and HTTP status 200 (OK) if the address is added successfully.
     *         If the request contains validation errors or the operation fails, appropriate error responses will be returned.
     */
    @PostMapping("/addAddress")
    public ResponseEntity<SuccessResponse> addAddress(@Valid @RequestBody AddressRequest addressRequest) {
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }


    /**
     * Endpoint to retrieve all addresses associated with a specific user.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing a list of AddressResponse objects and HTTP status 200 (OK) if the addresses are retrieved successfully.
     *         If the user ID is invalid or the operation fails, appropriate error responses will be returned.
     */
    @GetMapping("/getAddresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(HttpServletRequest request) {
        String userId = (String) request.getAttribute(Constants.USERID);
        List<AddressResponse> addressResponseList = userService.getAddresses(userId);
        return ResponseEntity.status(HttpStatus.OK).body(addressResponseList);
    }


    /**
     * Endpoint to update an existing address of a user.
     *
     * @param addressRequest The request object containing the updated address details (including addressId to identify which address to update).
     * @return A ResponseEntity containing the SuccessResponse object and HTTP status 200 (OK) if the address is updated successfully.
     *         If validation errors occur or the update operation fails, appropriate error responses will be returned.
     */
    @PutMapping("/updateAddress")
    public ResponseEntity<SuccessResponse> updateAddress(@Valid @RequestBody AddressRequest addressRequest) {
        SuccessResponse successResponse = userService.updateAddress(addressRequest);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }


    /**
     * Endpoint to delete a specific address of a user.
     *
     * @param request The HttpServletRequest containing the userId attribute.
     * @param addressId The ID of the address to be deleted.
     * @return A ResponseEntity containing the SuccessResponse object and HTTP status 200 (OK) if the address is deleted successfully.
     *         If the user or address ID is invalid, or if the deletion operation fails, appropriate error responses will be returned.
     */
    @DeleteMapping("/deleteAddress")
    public ResponseEntity<SuccessResponse> deleteAddress(HttpServletRequest request, @NotBlank @RequestParam String addressId) {
        String userId = (String) request.getAttribute(Constants.USERID);
        SuccessResponse successResponse = userService.deleteAddress(userId, addressId);
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    /**
     * Endpoint to add a new bank account for a user.
     *
     * @param request The UserBankDetailsRequest object containing the details of the bank account to be added.
     * @return A ResponseEntity containing the SuccessResponse object and HTTP status 200 (OK) if the bank account is added successfully.
     *         If the user does not exist, or if the bank account already exists, appropriate error responses will be returned.
     */
    @PostMapping("/addBankDetails")
    public ResponseEntity<SuccessResponse> addBankDetails(@Valid @RequestBody UserBankDetailsRequest request) {
        SuccessResponse successResponse = userService.addBankDetails(request);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

    /**
     * Endpoint to fetch all bank account details associated with a user.
     *
     * @param servletRequest The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing a list of UserBankDetailsResponse objects and HTTP status 200 (OK) if bank details are retrieved successfully.
     *         If no bank accounts are found, an empty list will be returned.
     */
    @GetMapping("/getAllUserBankDetails")
    public ResponseEntity<List<UserBankDetailsResponse>> getAllUserBankDetails(HttpServletRequest servletRequest) {
        String userId = (String) servletRequest.getAttribute(Constants.USERID);
        List<UserBankDetailsResponse> response = userService.getAllUserBankDetails(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint to delete a specific bank account for a user.
     *
     * @param userBankId The ID of the bank account to be deleted.
     * @param servletRequest The HttpServletRequest containing the userId attribute.
     * @return A ResponseEntity containing the SuccessResponse object and HTTP status 200 (OK) if the bank account is deleted successfully.
     *         If the bank account or user ID is invalid, or if the deletion operation fails, appropriate error responses will be returned.
     */
    @DeleteMapping("/deleteBankDetails")
    public ResponseEntity<SuccessResponse> deleteBankDetails(@Valid @RequestParam String userBankId, HttpServletRequest servletRequest) {
        String userId = (String) servletRequest.getAttribute(Constants.USERID);
        SuccessResponse successResponse = userService.deleteBankDetails(userBankId, userId);
        return ResponseEntity.status(successResponse.getStatusCode()).body(successResponse);
    }

}
