package com.cordestitch.controller.otp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.request.otp.OTPRequest;
import com.cordestitch.request.otp.ValidateOTP;
import com.cordestitch.response.otp.OTPResponse;
import com.cordestitch.service.service.otp.OTPService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(OTPController.class)
class OTPControllerTest {
    @MockBean
    public OTPService otpService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public IdEncryptor idEncryptor;

    @MockBean
    public JwtServiceImplementation jwtServiceImplementation;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void testVerifyOtp() throws Exception {
        ValidateOTP validateOTP = getValidOtp();
        OTPResponse otpResponse = new OTPResponse(Constants.OTP_VERIFIED_SUCCESSFULLY, HttpStatus.OK.value());
        when(otpService.verifyOtp(any())).thenReturn(otpResponse);
        mockMvc.perform(post("/api/otp/verifyOtp")
                        .content(objectMapper.writeValueAsString(validateOTP))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGenerateOtp() throws Exception {
        OTPRequest otpRequest = getOtpRequest();
        OTPResponse otpResponse = new OTPResponse(Constants.OTP_SUCCESS, HttpStatus.OK.value());
        when(otpService.generateOtp(any())).thenReturn(otpResponse);
        mockMvc.perform(post("/api/otp/sendOtp")
                        .content(objectMapper.writeValueAsString(otpRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private OTPRequest getOtpRequest() {
        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setPhoneNumber("6869601122");
        otpRequest.setUserType("customer");
        return otpRequest;
    }

    private ValidateOTP getValidOtp() {
        ValidateOTP validateOTP = new ValidateOTP();
        validateOTP.setUserType("customer");
        validateOTP.setOtpPassword("112233");
        validateOTP.setPhoneNumber("6869601122");
        return validateOTP;
    }

}