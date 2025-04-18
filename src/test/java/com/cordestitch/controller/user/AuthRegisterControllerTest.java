package com.cordestitch.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.user.*;
import com.cordestitch.request.user.UpdateProfileRequest;
import com.cordestitch.request.user.UpdateRequest;
import com.cordestitch.request.user.ValidateRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.otp.UpdateDataResponse;
import com.cordestitch.response.user.UserDetailsResponse;
import com.cordestitch.service.service.user.AuthRegisterService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthRegisterController.class)
class AuthRegisterControllerTest {
    @MockBean
    public AuthRegisterService authRegisterService;

    @Autowired
    public WebApplicationContext webApplicationContext;

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void testUpdateEmailAddressOrPhoneNumber() throws Exception{
        UpdateRequest updateRequest = getUpdateRequest();
        when(authRegisterService.updateEmailAddressOrPhoneNumber(any())).thenReturn(new SuccessResponse(Constants.SUCCESS, 200));
        mockMvc.perform(post("/api/auth/updateEmailAddressOrPhoneNumber")
                        .content(objectMapper.writeValueAsString(updateRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testValidateOTPForEmailOrPhoneNumber() throws Exception{
        ValidateRequest validateRequest = getValidateRequest();
        when(authRegisterService.validateOTPForEmailOrPhoneNumber(any())).thenReturn(new SuccessResponse(Constants.SUCCESS, 200));
        mockMvc.perform(post("/api/auth/validateOTPForEmailOrPhoneNumber")
                        .content(objectMapper.writeValueAsString(validateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckEmailOrPhoneNumberVerified() throws Exception{
        when(authRegisterService.checkEmailOrPhoneNumberVerified(any())).thenReturn(new UpdateDataResponse());
        mockMvc.perform(get("/api/auth/checkEmailOrPhoneNumberVerified")
                        .param("userId",ENCRYPTED_USER_ID)
                        .content(objectMapper.writeValueAsString(ENCRYPTED_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateProfile() throws Exception{
        UpdateProfileRequest updateProfileRequest = getUpdateProfileRequest();
        when(authRegisterService.updateProfile(any())).thenReturn(new SuccessResponse(Constants.SUCCESS, 200));
        mockMvc.perform(put("/api/auth/updateProfile")
                        .content(objectMapper.writeValueAsString(updateProfileRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserDetails() throws Exception{
        when(authRegisterService.getUserDetails(any())).thenReturn(new UserDetailsResponse());
        mockMvc.perform(get("/api/auth/getUserDetails")
                        .content(objectMapper.writeValueAsString(ENCRYPTED_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testUpdateUserDetails() throws Exception{
        UpdateUserDetailsRequest updateUserDetailsRequest = getUpdateUserDetailsRequest();
        when(authRegisterService.updateUserDetails(any())).thenReturn(new SuccessResponse(Constants.SUCCESS, 200));
        mockMvc.perform(put("/api/auth/updateUserDetails")
                        .content(objectMapper.writeValueAsString(updateUserDetailsRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private UpdateUserDetailsRequest getUpdateUserDetailsRequest() {
        UpdateUserDetailsRequest updateUserDetailsRequest = new UpdateUserDetailsRequest();
        updateUserDetailsRequest.setUserId(ENCRYPTED_USER_ID);
        updateUserDetailsRequest.setFirstName("Jay");
        updateUserDetailsRequest.setLastName("Prakash");
        updateUserDetailsRequest.setGender("male");
        updateUserDetailsRequest.setAddressRequests(new ArrayList<>());
        updateUserDetailsRequest.setEmailAddress("abc@gmail.com");
        return updateUserDetailsRequest;
    }

    private UpdateProfileRequest getUpdateProfileRequest() {
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setUserId(ENCRYPTED_USER_ID);
        updateProfileRequest.setFirstName("Jay");
        updateProfileRequest.setLastName("Prakash");
        updateProfileRequest.setGender("male");
        updateProfileRequest.setEmailAddress("jay@gmail.com");
        updateProfileRequest.setPhoneNumber("1234567890");
        return updateProfileRequest;
    }

    private ValidateRequest getValidateRequest() {
        ValidateRequest validateRequest = new ValidateRequest();
        validateRequest.setUserId(ENCRYPTED_USER_ID);
        validateRequest.setOtpCode("123456");
        validateRequest.setEmailAddress("jay@gmail.com");
        validateRequest.setPhoneNumber("1234567890");
        return validateRequest;
    }

    private UpdateRequest getUpdateRequest() {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setUserId(ENCRYPTED_USER_ID);
        updateRequest.setEmailAddress("newemail@gmail.com");
        updateRequest.setPhoneNumber("9876543210");
        return updateRequest;
    }
}