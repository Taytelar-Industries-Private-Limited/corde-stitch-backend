package com.cordestitch.serviceimplementation.otp;

import com.cordestitch.entity.admin.AdminEntity;
import com.cordestitch.entity.affiliate.AffiliateUserEntity;
import com.cordestitch.entity.otp.OTPEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.otp.InvalidOTPException;
import com.cordestitch.exception.otp.OtpNotFoundException;
import com.cordestitch.repository.admin.AdminRepository;
import com.cordestitch.repository.otp.OTPRepository;
import com.cordestitch.repository.user.AffiliateUserRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.otp.OTPRequest;
import com.cordestitch.request.otp.ValidateOTP;
import com.cordestitch.response.otp.OTPResponse;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.service.serviceimplementation.otp.OTPServiceImplementation;
import com.cordestitch.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OTPServiceImplementationTest {

    @InjectMocks
    private OTPServiceImplementation otpServiceImplementation;

    @Mock
    private OTPRepository otpRepository;

    @Mock
    private AffiliateUserRepository affiliateUserRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private UserRepository userRepository;


    @Mock
    private WhatsAppService whatsAppService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateOtp_Success_When_RequestType_Is_login_And_UserType_Is_Customer() throws IOException {
        OTPRequest otpRequest = getOtpRequest();

        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(userRepository.findUserByPhoneNumber(any())).thenReturn(new UserEntity());
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);

    }

    @Test
    void generateOtp_Success_When_RequestType_Is_login_And_UserType_Is_Affiliate() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType("affiliate");
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(affiliateUserRepository.findUserByPhoneNumber(any())).thenReturn(new AffiliateUserEntity());
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }

    @Test
    void generateOtp_Success_When_RequestType_Is_login_And_UserType_Is_Admin() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType("admin");
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(new AdminEntity());
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }

    @Test
    void generateOtp_Success_When_RequestType_Is_login_And_UserType_Is_Null() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType(null);
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(new AdminEntity());
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }


    @Test
    void generateOtp_Success_When_RequestType_Is_Register_And_UserType_Is_Customer() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(userRepository.findUserByPhoneNumber(any())).thenReturn(null);
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);

    }

    @Test
    void generateOtp_Success_When_RequestType_Is_Register_And_UserType_Affiliate() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType("affiliate");
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(affiliateUserRepository.findUserByPhoneNumber(any())).thenReturn(null);
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);

    }

    @Test
    void generateOtp_Success_When_RequestType_Is_Register_And_UserType_Admin() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType("admin");
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(null);
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }


    @Test
    void generateOtp_Success_When_RequestType_Is_Register_And_UserType_Is_Null() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType(null);
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(null);
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }

    @Test
    void generateOtp_Success_When_RequestType_Is_Register_And_UserType_Is_Null_OtpEntity_Not_Null() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType(null);
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        OTPEntity otpEntity = getOtpEntity();
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }

    @Test
    void generateOtp_Success_When_RequestType_Is_Register_And_UserType_Is_Null_OtpEntity_Not_Null_Contains_RequestType() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        otpRequest.setUserType("customer");
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", "Ob0rxjvg6qkgAKMztEjMKeKPHF9O7dTgTPJtRvb4cKqrWEWvOUqpoSDpmUmw");
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        OTPResponse otpResponse = getOtpResponse();
        OTPEntity otpEntity = getOtpEntity();
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        OTPResponse generatedOtp = spyService.generateOtp(otpRequest);
        assertEquals(otpResponse, generatedOtp);
    }


    @Test
    void generateOtp_Exception_While_Sending_OTP() throws IOException {
        OTPRequest otpRequest = getOtpRequest();
        when(userRepository.findUserByPhoneNumber(any())).thenReturn(new UserEntity());

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(401);
        OTPResponse response = otpServiceImplementation.generateOtp(otpRequest);
        assertEquals("Exception while sending OTP.. ", response.getMessage());
    }

    @Test
    void generateOtp_Exception_Failed_To_SendOtp() {
        OTPRequest otpRequest = getOtpRequest();
        ReflectionTestUtils.setField(otpServiceImplementation, "apiKey", null);
        ReflectionTestUtils.setField(otpServiceImplementation, "smsUrl", "https://www.fast2sms.com/dev/bulkV2?authorization=");

        when(userRepository.findUserByPhoneNumber(any())).thenReturn(new UserEntity());
        OTPResponse response = otpServiceImplementation.generateOtp(otpRequest);
        assertEquals(Constants.OTP_FAILED, response.getMessage());

    }

    @Test
    void verifyOtp_Success() {
        ValidateOTP validateOTP = getValidOtp();
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        OTPResponse response = otpServiceImplementation.verifyOtp(validateOTP);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void verifyOtp_Otp_Not_Found_Exception() {
        ValidateOTP validateOTP = getValidOtp();
        OtpNotFoundException exception = assertThrows(OtpNotFoundException.class, ()->otpServiceImplementation.verifyOtp(validateOTP));
        assertEquals(Constants.OTP_ENTITY_NOT_FOUND, exception.getMessage());
    }

    @Test
    void verifyOtp_Failed_UserType_false() {
        ValidateOTP validateOTP = new ValidateOTP();
        validateOTP.setOtpPassword("123456");
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        OTPResponse response = otpServiceImplementation.verifyOtp(validateOTP);
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void verifyOtp_Failed_OtpMismatch() {
        ValidateOTP validateOTP = getValidOtp();
        validateOTP.setOtpPassword(null);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        OTPResponse response = otpServiceImplementation.verifyOtp(validateOTP);
        assertEquals(400, response.getStatusCode());
    }

    @Test
    void testVerify_OTP_When_TimeIsExceeded() {
        ValidateOTP validateOTP = getValidOtp();
        validateOTP.setOtpPassword("123456");
        OTPEntity otpEntity = getOtpEntity();
        LocalDateTime time = LocalDateTime.now().minusMinutes(10);
        otpEntity.setOtpCreatedAt(time);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        assertThrows(InvalidOTPException.class, ()->otpServiceImplementation.verifyOtp(validateOTP));
    }

    private OTPEntity getOtpEntity() {
        OTPEntity otpEntity = new OTPEntity();
        otpEntity.setPhoneNumber("1234567890");
        otpEntity.setOtpVerified(true);
        otpEntity.setUserType("customer");
        otpEntity.setOtpCode("123456");
        otpEntity.setOtpCreatedAt(LocalDateTime.now());
        return otpEntity;
    }

    private ValidateOTP getValidOtp() {
        ValidateOTP validateOTP = new ValidateOTP();
        validateOTP.setUserType("customer");
        validateOTP.setOtpPassword("123456");
        validateOTP.setPhoneNumber("1234567890");
        return validateOTP;
    }

    private OTPResponse getOtpResponse() {
        OTPResponse otpResponse = new OTPResponse();
        otpResponse.setMessage("OTP sent successfully");
        otpResponse.setStatusCode(200);
        return otpResponse;
    }

    private OTPRequest getOtpRequest() {
        OTPRequest otpRequest = new OTPRequest();
        otpRequest.setUserType("customer");
        otpRequest.setPhoneNumber("1234567890");
        return otpRequest;
    }

}