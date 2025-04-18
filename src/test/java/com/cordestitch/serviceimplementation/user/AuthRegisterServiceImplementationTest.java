package com.cordestitch.serviceimplementation.user;

import com.cordestitch.entity.admin.AdminEntity;
import com.cordestitch.entity.affiliate.AffiliateUserEntity;
import com.cordestitch.entity.affiliate.SourceEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.otp.OTPEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.affiliate.FailedToSendOtpException;
import com.cordestitch.exception.otp.OtpNotFoundException;
import com.cordestitch.exception.user.UserDetailsMissMatchException;
import com.cordestitch.exception.user.UserNotFoundException;
import com.cordestitch.repository.admin.AdminRepository;
import com.cordestitch.repository.otp.OTPRepository;
import com.cordestitch.repository.user.AddressRepository;
import com.cordestitch.repository.user.AffiliateUserRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.user.*;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.otp.UpdateDataResponse;
import com.cordestitch.response.user.UserDetailsResponse;
import com.cordestitch.service.serviceimplementation.otp.OTPServiceImplementation;
import com.cordestitch.service.serviceimplementation.user.AuthRegisterServiceImplementation;
import com.cordestitch.service.serviceimplementation.user.UserServiceImplementation;
import com.cordestitch.util.Constants;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AuthRegisterServiceImplementationTest {
    @InjectMocks
    private AuthRegisterServiceImplementation authRegisterServiceImplementation;

    @Mock
    private UserServiceImplementation userServiceImplementation;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AffiliateUserRepository affiliateUserRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private OTPRepository otpRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private OTPServiceImplementation otpServiceImplementation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authRegisterServiceImplementation, "fromEmailAddress", "harsh@gmail.com");
    }

    @Test
    void testToGetUserDetails_Success() {
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(getListOfAddressEntity());
        UserDetailsResponse response = authRegisterServiceImplementation.getUserDetails("user");
        assertEquals("560064", response.getAddressResponses().getFirst().getPinCode());
    }

    @Test
    void testToGetUserDetails_When_UserEntity_IsNull() {
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> authRegisterServiceImplementation.getUserDetails("user"));
    }

    @Test
    void testToUpdateUserDetails_Success() {
        UpdateUserDetailsRequest request = getUpdateUserDetailsRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        SuccessResponse response = authRegisterServiceImplementation.updateUserDetails(request);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testToUpdateUserDetails_Success_When_EmailAddressIsNull() {
        UpdateUserDetailsRequest request = getUpdateUserDetailsRequest();
        request.setEmailAddress(null);
        request.setAddressRequests(null);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        SuccessResponse response = authRegisterServiceImplementation.updateUserDetails(request);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testToUpdateUserDetails_Success_When_EmailAddressIsEmpty() {
        UpdateUserDetailsRequest request = getUpdateUserDetailsRequest();
        request.setEmailAddress(null);
        request.setAddressRequests(Collections.emptyList());
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        SuccessResponse response = authRegisterServiceImplementation.updateUserDetails(request);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testToUpdateUserDetails_When_UserEntity_IsNull() {
        UpdateUserDetailsRequest request = getUpdateUserDetailsRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> authRegisterServiceImplementation.updateUserDetails(request));
    }

    @Test
    void testToUpdateUserDetails_When_OtpEntity_IsNull() {
        UpdateUserDetailsRequest request = getUpdateUserDetailsRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByPhoneNumber(any())).thenReturn(null);
        assertThrows(OtpNotFoundException.class, () -> authRegisterServiceImplementation.updateUserDetails(request));
    }

    private UpdateUserDetailsRequest getUpdateUserDetailsRequest() {
        UpdateUserDetailsRequest request = new UpdateUserDetailsRequest();
        request.setUserId("UID01");
        request.setFirstName("jay");
        request.setLastName("prakash");
        request.setGender("male");
        request.setEmailAddress("jay@example.com");
        request.setAddressRequests(List.of(getAddressRequest()));
        return request;
    }

    private AddressRequest getAddressRequest() {
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressId("10");
        addressRequest.setLandMark("near Government high school");
        addressRequest.setTypeOfAddress("home");
        addressRequest.setPinCode("560064");
        addressRequest.setStreetName("kogilu");
        addressRequest.setStateName("karnataka");
        addressRequest.setUserId("UID01");
        addressRequest.setBuildingName("ramanashree");
        addressRequest.setCityName("yelahanka");
        addressRequest.setCountryName("India");
        addressRequest.setFirstName("jay");
        addressRequest.setLastName("prakash");
        addressRequest.setPhoneNumber("1234567890");
        return addressRequest;
    }

    private List<AddressEntity> getListOfAddressEntity() {
        List<AddressEntity> addressEntities = new ArrayList<>();
        AddressEntity addressEntity = getAddressEntity();
        addressEntities.add(addressEntity);
        return addressEntities;
    }

    private AddressEntity getAddressEntity() {
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setAddressId("10");
        addressEntity.setLandMark("near Government high school");
        addressEntity.setTypeOfAddress("home");
        addressEntity.setPinCode("560064");
        addressEntity.setStreetName("kogilu");
        addressEntity.setStateName("karnataka");
        addressEntity.setUserEntity(getUserEntity());
        addressEntity.setOrderEntities(List.of(new OrderEntity()));
        addressEntity.setBuildingName("ramanashree");
        addressEntity.setCityName("yelahanka");
        addressEntity.setCountryName("India");
        addressEntity.setFirstName("jay");
        addressEntity.setLastName("prakash");
        addressEntity.setPhoneNumber("1234567890");
        return addressEntity;
    }


    @Test
    void updateEmailAddressOrPhoneNumber_Success(){
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        request.setPhoneNumber(null);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_When_Request_Is_Not_Null_For_Customer(){
        UpdateRequest request= getUpdateRequest();
        request.setPhoneNumber(null);
        request.setEmailAddress("kumar@gmail.com");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_When_Request_Is_Not_Null_For_Affiliate(){
        UpdateRequest request= getUpdateRequest();
        request.setPhoneNumber(null);
        request.setUserId("AID-123");
        request.setEmailAddress("kumar@gmail.com");
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(getAffiliateUserEntities()));
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_When_Request_Is_Not_Null_For_Affiliate(){
        UpdateRequest request= getUpdateRequest();
        request.setPhoneNumber(null);
        request.setUserId("AID-123");
        request.setEmailAddress("kumar@gmail.com");
        when(affiliateUserRepository.findByAffiliateUserId(null)).thenReturn(Optional.of(getAffiliateUserEntities()));
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        UserNotFoundException exception=assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_When_Request_Is_Not_Null_For_Admin(){
        UpdateRequest request= getUpdateRequest();
        request.setPhoneNumber(null);
        request.setUserId("AD-ID-123");
        request.setEmailAddress("kumar@gmail.com");
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(getAdminEntities()));
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_When_Request_Is_Not_Null_For_Admin(){
        UpdateRequest request= getUpdateRequest();
        request.setPhoneNumber(null);
        request.setUserId("AD-ID-123");
        request.setEmailAddress("kumar@gmail.com");
        when(adminRepository.findByAdminId(null)).thenReturn(Optional.of(getAdminEntities()));
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        UserNotFoundException exception=assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }


    @Test
    void updateEmailAddressOrPhoneNumber_Exception_When_Request_Is_Not_Null(){
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress("kumar@gmail.com");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.empty());
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        OtpNotFoundException exception=assertThrows(OtpNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.OTP_ENTITY_DATA_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_User_Not_Found_When_UserId_Is_Null(){
        UpdateRequest request= getUpdateRequest();
        request.setUserId(null);
        request.setEmailAddress("kumar@gmail.com");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        UserNotFoundException exception=assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_User_Not_Found_When_Request_Is_Not_Null(){
        UpdateRequest request= getUpdateRequest();
        request.setUserId("user");
        request.setEmailAddress("kumar@gmail.com");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        UserNotFoundException exception=assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_When_EmailAddress_Of_Request_And_Response_Is_Same(){
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress("jay@gmail.com");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        UserDetailsMissMatchException exception=assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.SAME_EMAIL_ERROR_MSG, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_When_PhoneNumber_Of_Request_And_Response_Is_Same(){
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress("kumar@gmail.com");
        request.setPhoneNumber("9876543210");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        UserDetailsMissMatchException exception=assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.SAME_PHONE_NUMBER_ERROR_MSG, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_SendToPhoneNumber_When_Request_Is_Not_Null_For_Customer() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        request.setUserId("UID-123");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.buildApiUrl(any(),any())).thenReturn("");
        when(otpServiceImplementation.sendOtp(any())).thenReturn(true);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertEquals(Constants.OTP_SUCCESS, response.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_SendToPhoneNumber_When_OtpFailed() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        request.setUserId("UID-123");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.buildApiUrl(any(),any())).thenReturn("");
        when(otpServiceImplementation.sendOtp(any())).thenReturn(false);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertEquals(Constants.OTP_FAILED, response.getMessage());
    }


    @Test
    void updateEmailAddressOrPhoneNumber_IOException_OR_SystemURIException_SendToPhoneNumber() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        request.setUserId("UID-123");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.sendOtp(any())).thenThrow(new IOException());
        FailedToSendOtpException exception = assertThrows(FailedToSendOtpException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.OTP_FAILED, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_SendToPhoneNumber_When_UserId_Is_Null() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        request.setUserId(null);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.buildApiUrl(any(),any())).thenReturn("");
        when(otpServiceImplementation.sendOtp(any())).thenReturn(true);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Exception_SendToPhoneNumber_When_OtpEntity_Is_Empty() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.buildApiUrl(any(),any())).thenReturn("");
        when(otpServiceImplementation.sendOtp(any())).thenReturn(true);
        OtpNotFoundException exception = assertThrows(OtpNotFoundException.class, ()->authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request));
        assertEquals(Constants.OTP_ENTITY_DATA_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_SendToPhoneNumber_When_Request_Is_Not_Null_For_Affiliate() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setEmailAddress(null);
        request.setUserId("AID-123");
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(getAffiliateUserEntities()));
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.buildApiUrl(any(),any())).thenReturn("");
        when(otpServiceImplementation.sendOtp(any())).thenReturn(true);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void updateEmailAddressOrPhoneNumber_Success_SendToPhoneNumber_When_Request_Is_Not_Null_For_Admin() throws Exception{
        UpdateRequest request= getUpdateRequest();
        request.setUserId("AD-ID-123");
        request.setEmailAddress(null);
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(getAdminEntities()));
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(otpServiceImplementation.buildApiUrl(any(),any())).thenReturn("");
        when(otpServiceImplementation.sendOtp(any())).thenReturn(true);
        SuccessResponse response=authRegisterServiceImplementation.updateEmailAddressOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Success_For_EmailAddress() {
        ValidateRequest request = getValidateRequest();
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("123456");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertEquals(Constants.OTP_VERIFIED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_For_EmailAddress() {
        ValidateRequest request = getValidateRequest();
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("654321");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.OTP_MISMATCH, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_For_EmailAddress_Is_Null() {
        ValidateRequest request = getValidateRequest();
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("654321");
        when(otpRepository.findByEmailAddress("kumar@gmail.com")).thenReturn(Optional.of(otpEntity));
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.EMAIL_MISMATCH, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Success_For_EmailAddress_For_Affiliate() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AID-123");
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("123456");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(getAffiliateUserEntities()));
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertEquals(Constants.OTP_VERIFIED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_User_Not_Found_For_EmailAddress_For_Affiliate() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AID-123");
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("123456");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        when(affiliateUserRepository.findByAffiliateUserId("AID-00")).thenReturn(Optional.of(getAffiliateUserEntities()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Success_For_EmailAddress_For_Admin() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AD-ID-123");
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("123456");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(getAdminEntities()));
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertEquals(Constants.OTP_VERIFIED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_User_Not_Found_For_EmailAddress_For_Admin() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AD-ID-123");
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("123456");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        when(adminRepository.findByAdminId("AD-ID-00")).thenReturn(Optional.of(getAdminEntities()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_User_Not_Found_For_EmailAddress_For_Unknown() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("UI123");
        request.setPhoneNumber(null);
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setEmailAddressOtpCode("123456");
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(otpEntity));
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertNotNull(response);
    }


    @Test
    void validateOTPForEmailOrPhoneNumber_Success_For_PhoneNumber() {
        ValidateRequest request = getValidateRequest();
        request.setEmailAddress(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertEquals(Constants.OTP_VERIFIED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_For_PhoneNumber() {
        ValidateRequest request = getValidateRequest();
        request.setEmailAddress(null);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber("110011")).thenReturn(otpEntity);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.PHONE_NUMBER_MISMATCH, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_For_PhoneNumber_When_UserEntity_Is_Null() {
        ValidateRequest request = getValidateRequest();
        request.setEmailAddress(null);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_Otp_Mismatch_For_PhoneNumber() {
        ValidateRequest request = getValidateRequest();
        request.setEmailAddress(null);
        request.setOtpCode("123123");
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.OTP_MISMATCH, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Success_For_PhoneNumber_For_Affiliate() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AID-123");
        request.setEmailAddress(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(getAffiliateUserEntities()));
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertEquals(Constants.OTP_VERIFIED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_User_Not_Found_For_PhoneNumber_For_Affiliate() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AID-123");
        request.setEmailAddress(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        when(affiliateUserRepository.findByAffiliateUserId("AID-00")).thenReturn(Optional.of(getAffiliateUserEntities()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Success_For_PhoneNumber_For_Admin() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AD-ID-123");
        request.setEmailAddress(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(getAdminEntities()));
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertEquals(Constants.OTP_VERIFIED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_User_Not_Found_For_PhoneNumber_For_Admin() {
        ValidateRequest request = getValidateRequest();
        request.setUserId("AD-ID-123");
        request.setEmailAddress(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        when(adminRepository.findByAdminId("AD-ID-00")).thenReturn(Optional.of(getAdminEntities()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Success_User_Not_Found_For_PhoneNumber_For_UserId_Is_Null() {
        ValidateRequest request = getValidateRequest();
        request.setUserId(null);
        request.setEmailAddress(null);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void validateOTPForEmailOrPhoneNumber_Exception_When_Both_EmailAddress_And_PhoneNumber_Are_Null() {
        ValidateRequest request = getValidateRequest();
        request.setPhoneNumber(null);
        request.setEmailAddress(null);
        when(otpRepository.findByEmailAddress(any())).thenReturn(Optional.of(getOtpEntity()));
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        SuccessResponse response = authRegisterServiceImplementation.validateOTPForEmailOrPhoneNumber(request);
        assertNotNull(response);
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Success_For_Unknown_User(){
        String userId = "UI-123";
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        UpdateDataResponse response = authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId);
        assertNotNull(response);
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Success_For_User(){
        String userId = "UID-123";
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        UpdateDataResponse response = authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId);
        assertNotNull(response);
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Exception_For_User(){
        String userId = "UID-123";
        when(userRepository.findUserByUserId(null)).thenReturn(getUserEntity());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Success_For_Affiliate(){
        String userId = "AID-123";
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(getAffiliateUserEntities()));
        UpdateDataResponse response = authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId);
        assertNotNull(response);
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Exception_For_Affiliate(){
        String userId = "AID-123";
        when(affiliateUserRepository.findByAffiliateUserId(null)).thenReturn(Optional.of(getAffiliateUserEntities()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Success_For_Admin(){
        String userId = "AD-ID-123";
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(getAdminEntities()));
        UpdateDataResponse response = authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId);
        assertNotNull(response);
    }

    @Test
    void checkEmailOrPhoneNumberVerified_Exception_For_Admin(){
        String userId = "AD-ID-123";
        when(adminRepository.findByAdminId(null)).thenReturn(Optional.of(getAdminEntities()));
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, ()->authRegisterServiceImplementation.checkEmailOrPhoneNumberVerified(userId));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateProfile_Success_For_Unknown_User(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("UI123");
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Success_For_User_When_Both_EmailAddress_And_PhoneNumber_Is_Not_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        UserEntity userEntity = getUserEntity();
        userEntity.setPhoneNumberVerified(false);
        userEntity.setEmailAddressVerified(false);
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Exception_For_User_When_Both_EmailAddress_And_PhoneNumber_Is_Not_Verified() throws Exception{
        UpdateProfileRequest request = getUpdateProfileRequest();
        Object mockEntity = mock(Object.class);
        Method method = AuthRegisterServiceImplementation.class.getDeclaredMethod(
                "checkSameDataOrNot", Object.class, UpdateProfileRequest.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class,
                () -> method.invoke(authRegisterServiceImplementation, mockEntity, request),
                Constants.SAME_DATA_ERROR_MSG);
    }

    @Test
    void updateProfile_Success_For_User_When_EmailAddress_Is_Verified_And_PhoneNumber_Is_Not_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        UserEntity userEntity = getUserEntity();
        userEntity.setPhoneNumberVerified(false);
        userEntity.setPhoneNumber("1234567890");
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Success_For_User_When_EmailAddress_Is_Not_Verified_And_PhoneNumber_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        UserEntity userEntity = getUserEntity();
        userEntity.setEmailAddressVerified(false);
        userEntity.setPhoneNumber("1234567890");
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Exception_Email_MisMatch_For_User_When_EmailAddress_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setEmailAddress("kaumar@gmail.com");
        UserEntity userEntity = getUserEntity();
        userEntity.setPhoneNumberVerified(false);
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateProfile(request));
        assertEquals(Constants.EMAIL_MISMATCH,exception.getMessage());
    }

    @Test
    void updateProfile_Exception_PhoneNumber_MisMatch_For_User_When_PhoneNumber_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateProfile(request));
        assertEquals(Constants.PHONE_NUMBER_MISMATCH,exception.getMessage());
    }

    @Test
    void updateProfile_Success_For_Admin_When_Both_EmailAddress_And_PhoneNumber_Is_Not_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AD-ID-123");
        AdminEntity userEntity = getAdminEntities();
        userEntity.setPhoneNumberVerified(false);
        userEntity.setEmailAddressVerified(false);
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(userEntity));
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Exception_For_Admin_When_Both_EmailAddress_And_PhoneNumber_Is_Not_Verified() throws Exception{
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AD-ID-123");
        Object mockEntity = mock(Object.class);
        Method method = AuthRegisterServiceImplementation.class.getDeclaredMethod(
                "checkSameDataOrNot", Object.class, UpdateProfileRequest.class);
        method.setAccessible(true);

        assertThrows(InvocationTargetException.class,
                () -> method.invoke(authRegisterServiceImplementation, mockEntity, request),
                Constants.SAME_DATA_ERROR_MSG);
    }

    @Test
    void updateProfile_Success_For_Admin_When_EmailAddress_Is_Verified_And_PhoneNumber_Is_Not_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AD-ID-123");
        AdminEntity userEntity = getAdminEntities();
        userEntity.setPhoneNumberVerified(false);
        userEntity.setPhoneNumber("1234567890");
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(userEntity));
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Success_For_Admin_When_EmailAddress_Is_Not_Verified_And_PhoneNumber_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AD-ID-123");
        AdminEntity userEntity = getAdminEntities();
        userEntity.setEmailAddressVerified(false);
        userEntity.setPhoneNumber("1234567890");
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(userEntity));
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Exception_Email_MisMatch_For_Admin_When_EmailAddress_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AD-ID-123");
        request.setEmailAddress("kaumar@gmail.com");
        AdminEntity userEntity = getAdminEntities();
        userEntity.setPhoneNumberVerified(false);
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(userEntity));
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateProfile(request));
        assertEquals(Constants.EMAIL_MISMATCH,exception.getMessage());
    }

    @Test
    void updateProfile_Exception_PhoneNumber_MisMatch_For_Admin_When_PhoneNumber_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AD-ID-123");
        AdminEntity userEntity = getAdminEntities();
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(userEntity));
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateProfile(request));
        assertEquals(Constants.PHONE_NUMBER_MISMATCH,exception.getMessage());
    }



    @Test
    void updateProfile_Success_For_Affiliate_When_Both_EmailAddress_And_PhoneNumber_Is_Not_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AID-123");
        AffiliateUserEntity userEntity = getAffiliateUserEntities();
        userEntity.setPhoneNumberVerified(false);
        userEntity.setEmailAddressVerified(false);
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(userEntity));
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Exception_For_Affiliate_When_Both_EmailAddress_And_PhoneNumber_Is_Not_Verified() throws Exception{
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AID-123");
        Object mockEntity = mock(Object.class);
        Method method = AuthRegisterServiceImplementation.class.getDeclaredMethod(
                "checkSameDataOrNot", Object.class, UpdateProfileRequest.class);
        method.setAccessible(true);

        assertThrows(InvocationTargetException.class,
                () -> method.invoke(authRegisterServiceImplementation, mockEntity, request),
                Constants.SAME_DATA_ERROR_MSG);
    }

    @Test
    void updateProfile_Success_For_Affiliate_When_EmailAddress_Is_Verified_And_PhoneNumber_Is_Not_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AID-123");
        AffiliateUserEntity userEntity = getAffiliateUserEntities();
        userEntity.setPhoneNumberVerified(false);
        userEntity.setPhoneNumber("1234567890");
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(userEntity));
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Success_For_Affiliate_When_EmailAddress_Is_Not_Verified_And_PhoneNumber_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AID-123");
        AffiliateUserEntity userEntity = getAffiliateUserEntities();
        userEntity.setEmailAddressVerified(false);
        userEntity.setPhoneNumber("1234567890");
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(userEntity));
        SuccessResponse response = authRegisterServiceImplementation.updateProfile(request);
        assertNotNull(response);
    }

    @Test
    void updateProfile_Exception_Email_MisMatch_For_Affiliate_When_EmailAddress_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AID-123");
        request.setEmailAddress("kaumar@gmail.com");
        AffiliateUserEntity userEntity = getAffiliateUserEntities();
        userEntity.setPhoneNumberVerified(false);
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(userEntity));
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateProfile(request));
        assertEquals(Constants.EMAIL_MISMATCH,exception.getMessage());
    }

    @Test
    void updateProfile_Exception_PhoneNumber_MisMatch_For_Affiliate_When_PhoneNumber_Is_Verified(){
        UpdateProfileRequest request = getUpdateProfileRequest();
        request.setUserId("AID-123");
        AffiliateUserEntity userEntity = getAffiliateUserEntities();
        when(affiliateUserRepository.findByAffiliateUserId(any())).thenReturn(Optional.of(userEntity));
        UserDetailsMissMatchException exception = assertThrows(UserDetailsMissMatchException.class, ()->authRegisterServiceImplementation.updateProfile(request));
        assertEquals(Constants.PHONE_NUMBER_MISMATCH,exception.getMessage());
    }

    private UpdateProfileRequest getUpdateProfileRequest() {
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setEmailAddress("jay@gmail.com");
        updateProfileRequest.setGender("male");
        updateProfileRequest.setPhoneNumber("1234567890");
        updateProfileRequest.setUserId("UID-123");
        updateProfileRequest.setFirstName("jay");
        updateProfileRequest.setLastName("prakash");
        return updateProfileRequest;
    }

    private ValidateRequest getValidateRequest() {
        ValidateRequest validateRequest = new ValidateRequest();
        validateRequest.setEmailAddress("jay@gmail.com");
        validateRequest.setPhoneNumber("1234567890");
        validateRequest.setUserId("UID-123");
        validateRequest.setOtpCode("123456");
        return validateRequest;
    }


    private AdminEntity getAdminEntities() {
        AdminEntity adminEntity = new AdminEntity();
        adminEntity.setPhoneNumber("9999999999");
        adminEntity.setReferralCode("ABC123");
        adminEntity.setEmailAddress("jay@gmail.com");
        adminEntity.setUserType("admin");
        adminEntity.setAuthenticationSource("google");
        adminEntity.setEmailAddressVerified(true);
        adminEntity.setUserCreatedAt(LocalDateTime.now());
        adminEntity.setAdminId("A1");
        adminEntity.setFirstName("jay");
        adminEntity.setLastName("prakash");
        adminEntity.setReferredReferralCode("ABC123");
        adminEntity.setPhoneNumberVerified(true);
        return adminEntity;
    }

    private AffiliateUserEntity getAffiliateUserEntities() {
        AffiliateUserEntity affiliateUserEntity = new AffiliateUserEntity();
        affiliateUserEntity.setPhoneNumber("9999999999");
        affiliateUserEntity.setReferralCode("ABC123");
        affiliateUserEntity.setEmailAddress("jay@gmail.com");
        affiliateUserEntity.setUserType("affiliate");
        affiliateUserEntity.setAuthenticationSource("google");
        affiliateUserEntity.setEmailAddressVerified(true);
        affiliateUserEntity.setUserCreatedAt(LocalDateTime.now());
        affiliateUserEntity.setAffiliateUserId("A1");
        affiliateUserEntity.setFirstName("jay");
        affiliateUserEntity.setLastName("prakash");
        affiliateUserEntity.setSourceEntities(List.of(new SourceEntity()));
        affiliateUserEntity.setPhoneNumberVerified(true);
        affiliateUserEntity.setReferredReferralCode("ABC123");
        return affiliateUserEntity;
    }

    private UpdateRequest getUpdateRequest() {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setEmailAddress("jay@gmail.com");
        updateRequest.setUserId("UID-123");
        updateRequest.setPhoneNumber("1234567890");
        return updateRequest;
    }

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("Id1");
        userEntity.setFirstName("jay");
        userEntity.setLastName("prakash");
        userEntity.setPhoneNumber("9876543210");
        userEntity.setEmailAddress("jay@gmail.com");
        userEntity.setReferralCode("123456");
        userEntity.setEmailAddressVerified(true);
        userEntity.setPhoneNumberVerified(true);
        userEntity.setReferredReferralCode("123456");
        userEntity.setUserCreatedAt(LocalDateTime.now());
        userEntity.setAuthenticationSource("google");
        return userEntity;
    }

    private OTPEntity getOtpEntity() {
        OTPEntity otpEntity = new OTPEntity();
        otpEntity.setPhoneNumber("1234567890");
        otpEntity.setOtpVerified(true);
        otpEntity.setUserType("customer");
        otpEntity.setOtpCode("123456");
        return otpEntity;
    }
}