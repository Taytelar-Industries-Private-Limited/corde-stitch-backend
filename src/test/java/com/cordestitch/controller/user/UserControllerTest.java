package com.cordestitch.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.request.user.LoginRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.response.user.LoginResponse;
import com.cordestitch.service.service.user.UserService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockBean
    public UserService userService;

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
    void testLogin() throws Exception {
        LoginRequest loginRequest = getLoginRequest();
        LoginResponse loginResponse = new LoginResponse();
        when(userService.login(any(), any(), any())).thenReturn(loginResponse);
        mockMvc.perform(post("/api/user/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAddAddress() throws Exception {
        AddressRequest addressRequest = getAddressRequest();
        SuccessResponse successResponse = getSuccessResponse();
        when(userService.addAddress(any())).thenReturn(successResponse);
        mockMvc.perform(post("/api/user/addAddress")
                        .content(objectMapper.writeValueAsString(addressRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAddresses() throws Exception {
        when(userService.getAddresses(ENCRYPTED_USER_ID)).thenReturn(getListAddressResponse());
        mockMvc.perform(get("/api/user/getAddresses")
                        .param("userId", ENCRYPTED_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateAddress() throws Exception {
        AddressRequest addressRequest = getAddressRequest();
        SuccessResponse successResponse = getSuccessResponse();
        when(userService.updateAddress(any())).thenReturn(successResponse);
        mockMvc.perform(put("/api/user/updateAddress")
                        .content(objectMapper.writeValueAsString(addressRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testDeleteAddress() throws Exception {
        String addressId = "1";
        when(userService.deleteAddress(ENCRYPTED_USER_ID, addressId)).thenReturn(getSuccessResponse());
        mockMvc.perform(delete("/api/user/deleteAddress")
                        .param("userId", ENCRYPTED_USER_ID)
                        .param("addressId", addressId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testLogout() throws Exception {
        when(userService.logout(any())).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/user/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private List<AddressResponse> getListAddressResponse() {
        List<AddressResponse> addressResponseList = new ArrayList<>();
        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setAddressId("1");
        addressResponse.setTypeOfAddress("home");
        addressResponse.setCityName("bangalore");
        addressResponse.setCountryName("india");
        addressResponse.setBuildingName("Prestige");
        addressResponse.setLandMark("near Government School");
        addressResponse.setPinCode("560064");
        addressResponse.setStateName("karnataka");
        addressResponse.setStreetName("church street");
        addressResponse.setFirstName("Hemanth");
        addressResponse.setLastName("kumar");
        addressResponseList.add(addressResponse);
        return addressResponseList;
    }

    private SuccessResponse getSuccessResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage("Address added successfully");
        successResponse.setStatusCode(200);
        return successResponse;
    }

    private AddressRequest getAddressRequest() {
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressId("1");
        addressRequest.setUserId(ENCRYPTED_USER_ID);
        addressRequest.setPhoneNumber("1234567890");
        addressRequest.setTypeOfAddress("home");
        addressRequest.setCityName("bangalore");
        addressRequest.setCountryName("india");
        addressRequest.setBuildingName("Prestige");
        addressRequest.setLandMark("near Government School");
        addressRequest.setPinCode("560064");
        addressRequest.setStateName("karnataka");
        addressRequest.setStreetName("church street");
        addressRequest.setFirstName("Hemanth");
        addressRequest.setLastName("kumar");
        return addressRequest;
    }

    private LoginRequest getLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhoneNumber("9900554400");
        loginRequest.setUserType("customer");
        return loginRequest;
    }

}