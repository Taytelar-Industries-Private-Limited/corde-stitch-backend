package com.cordestitch.controller.customization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.customization.usercustomization.CustomizedAddDataRequest;
import com.cordestitch.request.customization.usercustomization.CustomizedCartItemRequest;
import com.cordestitch.request.customization.usercustomization.CustomizedDataAndCartRequest;
import com.cordestitch.request.customization.usercustomization.UpdateCartItemRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.service.service.customization.UserCustomizationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserCustomizationController.class)
class UserCustomizationControllerTest {
    @MockBean
    public UserCustomizationService userCustomizationService;

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
    void testAddCustomizedDataToCart() throws Exception {
        CustomizedDataAndCartRequest request = getCustomizedDataAndCartRequest();
        when(userCustomizationService.addCustomizedDataToCart(any(),any(),any())).thenReturn(new SuccessResponse());
        mockMvc.perform(post("/api/userCustomization/addCustomizedDataToCart")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateCustomizedCartItem() throws Exception {
        UpdateCartItemRequest request = getUpdateCartItemRequest();
        when(userCustomizationService.updateCustomizedCartItem(any())).thenReturn(new SuccessResponse());
        mockMvc.perform(patch("/api/userCustomization/updateCustomizedCartItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCustomizedCartItem() throws Exception {
        String customizedCartItemId="customId12";
        when(userCustomizationService.deleteCustomizedCartItem(any(),any())).thenReturn(new SuccessResponse());
        mockMvc.perform(delete("/api/userCustomization/deleteCustomizedCartItem")
                        .param("userId",ENCRYPTED_USER_ID)
                        .param("customizedCartItemId",customizedCartItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private UpdateCartItemRequest getUpdateCartItemRequest() {
        UpdateCartItemRequest request=new UpdateCartItemRequest();
        request.setUserId(ENCRYPTED_USER_ID);
        request.setCustomizedCartItemId("customId12");
        request.setQuantity(2);
        return request;
    }


    private CustomizedDataAndCartRequest getCustomizedDataAndCartRequest() throws Exception {
        CustomizedDataAndCartRequest request = new CustomizedDataAndCartRequest();
        request.setUserId(ENCRYPTED_USER_ID);
        request.setCustomizedData(getCustomizedDataRequest());
        request.setCustomizedCartItemRequest(getCustomizedCartItemRequest());
        return request;
    }

    private CustomizedCartItemRequest getCustomizedCartItemRequest() {
        CustomizedCartItemRequest request = new CustomizedCartItemRequest();
        request.setQuantity(2);
        return request;
    }

    private CustomizedAddDataRequest getCustomizedDataRequest() throws Exception {
        String fabricDetailsJson = "{ \"material\": \"cotton\", \"weight\": \"light\", \"color\": \"blue\" }";
        JsonNode fabricDetails = objectMapper.readTree(fabricDetailsJson);
        CustomizedAddDataRequest customizationRequest = new CustomizedAddDataRequest();
        customizationRequest.setFlyType("flyType1");
        customizationRequest.setFitType("fitType1");
        customizationRequest.setPantCuffsType("ribben");
        customizationRequest.setPantType("formal");
        customizationRequest.setRiseType("rise");
        customizationRequest.setPantInSeamLength(15);
        customizationRequest.setPantOutSeamLength(13);
        customizationRequest.setPantPleatType("single");
        customizationRequest.setTrueWaistMeasurement(30);
        customizationRequest.setFrontPocketType("box");
        customizationRequest.setBackPocketType("button");
        customizationRequest.setBackButtonType("round");
        customizationRequest.setFrontButtonType("hexagon");
        customizationRequest.setFabricDetails(fabricDetails);
        return customizationRequest;
    }

}