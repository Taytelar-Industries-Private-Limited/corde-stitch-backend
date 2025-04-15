package com.cordestitch.serviceimplementation.user;

import com.cordestitch.entity.admin.AdminEntity;
import com.cordestitch.entity.affiliate.AffiliateUserEntity;
import com.cordestitch.entity.affiliate.SourceEntity;
import com.cordestitch.entity.cart.CartDataEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.entity.order.OrderEntity;
import com.cordestitch.entity.otp.OTPEntity;
import com.cordestitch.entity.payment.CardEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserBankAccountEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.otp.OtpNotFoundException;
import com.cordestitch.exception.otp.UnknownUserTypeException;
import com.cordestitch.exception.payment.RazorpayDataException;
import com.cordestitch.exception.user.*;
import com.cordestitch.repository.admin.AdminRepository;
import com.cordestitch.repository.cart.CartDataRepository;
import com.cordestitch.repository.otp.OTPRepository;
import com.cordestitch.repository.user.AddressRepository;
import com.cordestitch.repository.user.AffiliateUserRepository;
import com.cordestitch.repository.user.UserBankDetailsRepository;
import com.cordestitch.repository.user.UserRepository;
import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.request.user.LoginRequest;
import com.cordestitch.request.user.UserBankDetailsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.response.user.LoginResponse;
import com.cordestitch.response.user.UserBankDetailsResponse;
import com.cordestitch.service.service.payment.PaymentService;
import com.cordestitch.service.serviceimplementation.cart.CartDataServiceImplementation;
import com.cordestitch.service.serviceimplementation.cart.CartServiceImplementation;
import com.cordestitch.service.serviceimplementation.payment.PaymentServiceHelper;
import com.cordestitch.service.serviceimplementation.token.CookieService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.service.serviceimplementation.user.UserServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplementationTest {
    @InjectMocks
    private UserServiceImplementation userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AffiliateUserRepository affiliateUserRepository;

    @Mock
    private OTPRepository otpRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CookieService cookieService;

    @Mock
    private HttpServletResponse servletResponse;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private JwtServiceImplementation jwtServiceImplementation;

    @Mock
    private Generator generator;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserBankDetailsRepository userBankDetailsRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentServiceHelper paymentServiceHelper;

    @Mock
    private CartDataServiceImplementation cartDataServiceImplementation;

    @Mock
    private CartDataRepository cartDataRepository;

    @Mock
    private CartServiceImplementation cartServiceImplementation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void userLogin_When_Otp_IsNotVerified() {
        LoginRequest request = getLoginRequest();
        OTPEntity otpEntity = getOtpEntity();
        otpEntity.setOtpVerified(false);
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        assertThrows(OtpNotFoundException.class, ()-> userService.login(request, httpServletRequest, servletResponse));
    }

    @Test
    void userLogin_When_OtpDataNotFound() {
        LoginRequest request = getLoginRequest();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(null);
        assertThrows(OtpNotFoundException.class, ()-> userService.login(request, httpServletRequest, servletResponse));
    }

    @Test
    void userLogin_When_UserType_IsCustomer() {
        LoginRequest request = getLoginRequest();
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(userRepository.findUserByPhoneNumber(any())).thenReturn(getUserEntity());
        when(cartDataRepository.findByDeviceIdAndTokenId(any(), any())).thenReturn(getCartDataEntity());
        when(cartServiceImplementation.addToCart(any())).thenReturn(new SuccessResponse());
        LoginResponse response = userService.login(request, httpServletRequest, servletResponse);
        assertEquals("jay@gmail.com", response.getEmailAddress());
    }

    @Test
    void newUserLogin_When_UserType_IsCustomer() {
        LoginRequest request = getLoginRequest();
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(userRepository.findUserByPhoneNumber(any())).thenReturn(null);
        LoginResponse response = userService.login(request, httpServletRequest, servletResponse);
        assertEquals("1234567890", response.getPhoneNumber());
    }

    @Test
    void userLogin_When_UserType_IsAffiliate() {
        LoginRequest request = getLoginRequest();
        request.setUserType(Constants.AFFILIATE);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(affiliateUserRepository.findUserByPhoneNumber(any())).thenReturn(getAffiliateUserEntity());
        LoginResponse response = userService.login(request, httpServletRequest, servletResponse);
        assertEquals("jay@gmail.com", response.getEmailAddress());
    }

    @Test
    void newUserLogin_When_UserType_IsAffiliate() {
        LoginRequest request = getLoginRequest();
        request.setUserType(Constants.AFFILIATE);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(affiliateUserRepository.findUserByPhoneNumber(any())).thenReturn(null);
        LoginResponse response = userService.login(request, httpServletRequest, servletResponse);
        assertEquals("1234567890", response.getPhoneNumber());
    }

    @Test
    void userLogin_When_UserType_IsAdmin() {
        LoginRequest request = getLoginRequest();
        request.setUserType(Constants.ADMIN);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(getAdminEntity());
        LoginResponse response = userService.login(request, httpServletRequest, servletResponse);
        assertEquals("jay@gmail.com", response.getEmailAddress());
    }

    @Test
    void newUserLogin_When_UserType_IsAdmin() {
        LoginRequest request = getLoginRequest();
        request.setUserType(Constants.ADMIN);
        OTPEntity otpEntity = getOtpEntity();
        when(otpRepository.findByPhoneNumber(any())).thenReturn(otpEntity);
        when(adminRepository.findUserByPhoneNumber(any())).thenReturn(null);
        LoginResponse response = userService.login(request, httpServletRequest, servletResponse);
        assertEquals("1234567890", response.getPhoneNumber());
    }

    @Test
    void userLogin_When_UserType_IsInvalid() {
        LoginRequest request = getLoginRequest();
        request.setUserType("Invalid");
        when(otpRepository.findByPhoneNumber(any())).thenReturn(getOtpEntity());
        assertThrows(UnknownUserTypeException.class, ()-> userService.login(request, httpServletRequest, servletResponse));
    }

    @Test
    void testLogOut() {
        SuccessResponse response = userService.logout(servletResponse);
        assertEquals(Constants.LOGOUT_SUCCESSFULLY, response.getMessage());
    }


    @Test
    void addAddress_Success() {
        AddressRequest addressRequest = getAddressRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.save(any(AddressEntity.class))).thenReturn(getAddressEntity());
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());

    }

    @Test
    void testAddAddress_When_AddAddress_Limit_Exceeds() {
        AddressRequest addressRequest = getAddressRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(new ArrayList<>(Collections.nCopies(6, getAddressEntity())));
        assertThrows(AddressLimitExceededException.class, () -> userService.addAddress(addressRequest));
    }

    @Test
    void addAddress_When_AddressId_Is_Null() {
        AddressRequest addressRequest = getAddressRequest();
        addressRequest.setAddressId(null);
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.save(any(AddressEntity.class))).thenReturn(getAddressEntity());
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressId_Is_Empty() {
        AddressRequest addressRequest = getAddressRequest();
        addressRequest.setAddressId("");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.save(any(AddressEntity.class))).thenReturn(getAddressEntity());
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void checkLandMark_ReturnsTrue_WhenBothLandMarksMatchAndNotNull() {
        AddressRequest addressRequest = getAddressRequest();
        addressRequest.setLandMark("Near School");
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setLandMark("Near School");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        AddressAlreadyExistsException exception = assertThrows(AddressAlreadyExistsException.class, () -> userService.addAddress(addressRequest));
        assertEquals(Constants.ADDRESS_ALREADY_EXIST, exception.getMessage());

    }

    @ParameterizedTest
    @CsvSource({
            "Near School, Near Hospital",    // Different landmarks
            "Near School, null",            // Request landmark non-null, entity landmark null
            "null, Near Hospital",          // Request landmark null, entity landmark non-null
            "null, null",                   // Both landmarks null
            "Near School, ''"               // Request landmark non-null, entity landmark empty string
    })
    void checkLandMark_ReturnsSuccess(String requestLandMark, String entityLandMark) {
        AddressRequest addressRequest = getAddressRequest();
        addressRequest.setLandMark(requestLandMark);

        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setLandMark(entityLandMark.equals("null") ? null : entityLandMark);

        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);

        SuccessResponse successResponse = userService.addAddress(addressRequest);

        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_Exception_When_Address_Already_Exists() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        AddressAlreadyExistsException exception = assertThrows(AddressAlreadyExistsException.class, () -> userService.addAddress(addressRequest));
        assertEquals(Constants.ADDRESS_ALREADY_EXIST, exception.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_FirstName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setFirstName("harish");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_LastName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setLastName("krishna");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_PhoneNumber_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setPhoneNumber("0987654321");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_BuildingName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setBuildingName("gopal complex");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_StreetName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setStreetName("church street");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_CityName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setCityName("hebbal");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_StateName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setStateName("kerala");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_CountryName_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setCountryName("England");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_PinCode_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setPinCode("571103");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_TypeOfAddress_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setTypeOfAddress("office");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_When_AddressEntities_And_LandMark_Request_Is_Not_Equal() {
        AddressRequest addressRequest = getAddressRequest();
        List<AddressEntity> addressEntities = getListAddressEntities();
        addressEntities.getFirst().setLandMark("near Iskcon temple");
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        SuccessResponse successResponse = userService.addAddress(addressRequest);
        assertEquals(Constants.ADDRESS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void addAddress_Exception_When_User_Not_Found() {
        AddressRequest addressRequest = getAddressRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.addAddress(addressRequest));
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void getAddresses_Success() {
        String userID = "UID01";
        UserEntity userEntity = getUserEntity();
        List<AddressEntity> addressEntities = getListAddressEntities();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndIsDeletedFalse(any())).thenReturn(addressEntities);
        List<AddressResponse> addressResponses = userService.getAddresses(userID);
        assertNotNull(addressResponses);
        verify(addressRepository).findByUserEntityUserIdAndIsDeletedFalse(userID);
    }

    @Test
    void updateAddress_Success() {
        AddressRequest addressRequest = getAddressRequest();
        UserEntity userEntity = getUserEntity();
        AddressEntity addressEntity = getAddressEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(addressEntity);
        SuccessResponse successResponse = userService.updateAddress(addressRequest);
        assertEquals(Constants.ADDRESS_UPDATED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void updateAddress_Exception_Address_Not_Found() {
        AddressRequest addressRequest = getAddressRequest();
        UserEntity userEntity = getUserEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(null);
        AddressNotFoundException addressNotFoundException = assertThrows(AddressNotFoundException.class, () -> userService.updateAddress(addressRequest));
        assertEquals(Constants.ADDRESS_NOT_FOUND, addressNotFoundException.getMessage());
    }

    @Test
    void deleteAddress_Success() {
        String userId = "UID1";
        String addressID = "10";
        UserEntity userEntity = getUserEntity();
        AddressEntity addressEntity = getAddressEntity();
        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(addressRepository.findByUserEntityUserIdAndAddressId(any(), any())).thenReturn(addressEntity);
        SuccessResponse successResponse = userService.deleteAddress(userId, addressID);
        assertEquals(Constants.ADDRESS_DELETED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testAddBankDetails_Success() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        UserEntity userEntity = getUserEntity();
        UserBankAccountEntity savedEntity = getNewUserBankAccountEntity();

        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(userBankDetailsRepository.save(any())).thenReturn(savedEntity);
        when(modelMapper.map(request, UserBankAccountEntity.class)).thenReturn(savedEntity);
        when(paymentService.createRazorpayContact(any(), any(), any())).thenReturn("cont_uhbjh2hg3u7bvj");
        when(paymentService.createFundAccount(any(), any())).thenReturn("fund_8uebjhb2vsj");

        SuccessResponse successResponse = userService.addBankDetails(request);
        assertEquals(Constants.BANK_DETAILS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testAddBankDetails_Success_NewAccount_Adding() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        request.setBankName("SBM");
        request.setAccountHolderName("Harshal");
        request.setAccountNumber("990088776655");
        request.setBankIfscCode("SBM000123");
        UserEntity userEntity = getUserEntity();
        UserBankAccountEntity savedEntity = getNewUserBankAccountEntity();

        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(getListOfBankDetails(userEntity));
        when(userBankDetailsRepository.save(any())).thenReturn(savedEntity);
        when(modelMapper.map(request, UserBankAccountEntity.class)).thenReturn(savedEntity);
        when(paymentService.createRazorpayContact(any(), any(), any())).thenReturn("cont_uhbjh2hg3u7bvj");
        when(paymentService.createFundAccount(any(), any())).thenReturn("fund_8uebjhb2vsj");

        SuccessResponse successResponse = userService.addBankDetails(request);
        assertEquals(Constants.BANK_DETAILS_ADDED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testAddBankDetails_When_Already_Deleted_Account_Adding_Again_But_Limit_Exceeds() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        UserEntity userEntity = getUserEntity();
        List<UserBankAccountEntity> userBankAccountEntities = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserBankAccountEntity activeAccount = getUserBankAccountEntity(userEntity);
            activeAccount.setIsBankAccountDeleted(false);
            userBankAccountEntities.add(activeAccount);
        }
        UserBankAccountEntity deletedAccount = getUserBankAccountEntity(userEntity);
        deletedAccount.setIsBankAccountDeleted(true);
        userBankAccountEntities.add(deletedAccount);

        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(userBankAccountEntities);
        RazorpayDataException exception = assertThrows(RazorpayDataException.class, () -> userService.addBankDetails(request));
        assertEquals(Constants.MAX_BANK_ACCOUNTS_REACHED, exception.getMessage());
    }

    @Test
    void testAddBankDetails_When_Already_Deleted_Account_Adding_Again() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        UserEntity userEntity = getUserEntity();
        List<UserBankAccountEntity> userBankAccountEntities = getListOfBankDetails(userEntity);
        userBankAccountEntities.getFirst().setIsBankAccountDeleted(true);

        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(userBankAccountEntities);
        when(paymentServiceHelper.sendHttpRequest(anyString(),any(), any(), anyString())).thenReturn(getTrueResponse());
        SuccessResponse successResponse = userService.addBankDetails(request);
        assertEquals(Constants.BANK_DETAILS_RESTORED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testAddBankDetails_When_Already_Deleted_Account_Adding_Again_When_ContactResponse_IsNull() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        UserEntity userEntity = getUserEntity();
        List<UserBankAccountEntity> userBankAccountEntities = getListOfBankDetails(userEntity);
        userBankAccountEntities.getFirst().setIsBankAccountDeleted(true);

        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(userBankAccountEntities);
        when(paymentServiceHelper.sendHttpRequest(anyString(),any(), any(), anyString())).thenReturn(null);
        assertThrows(RazorpayDataException.class, ()-> userService.addBankDetails(request));
    }

    @Test
    void testAddBankDetails_When_Already_Deleted_Account_Adding_Again_When_FundAccountResponse_IsNull() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        UserEntity userEntity = getUserEntity();
        List<UserBankAccountEntity> userBankAccountEntities = getListOfBankDetails(userEntity);
        userBankAccountEntities.getFirst().setIsBankAccountDeleted(true);

        when(userRepository.findUserByUserId(any())).thenReturn(userEntity);
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(userBankAccountEntities);
        when(paymentServiceHelper.sendHttpRequest(anyString(),any(), any(), anyString())).thenReturn(getTrueResponse()).thenReturn(null);
        assertThrows(RazorpayDataException.class, ()-> userService.addBankDetails(request));
    }

    @Test
    void testAddBankDetails_When_AddAccount_Limit_Exceeds() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        request.setAccountNumber("098765432123");
        List<UserBankAccountEntity> entities = new ArrayList<>(Collections.nCopies(4, getUserBankAccountEntity(getUserEntity())));

        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(entities);
        RazorpayDataException exception = assertThrows(RazorpayDataException.class, () -> userService.addBankDetails(request));
        assertEquals(Constants.MAX_BANK_ACCOUNTS_REACHED, exception.getMessage());
    }

    @Test
    void testAddBankDetails_When_Account_Already_Exists() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(getUserEntity());
        when(userBankDetailsRepository.findByUserEntityUserId(any())).thenReturn(getListOfBankDetails(getUserEntity()));
        assertThrows(AccountNumberAlreadyExistsException.class, ()->userService.addBankDetails(request));
    }

    @Test
    void testAddBankDetails_When_User_Not_Found() {
        UserBankDetailsRequest request = getUserBankDetailsRequest();
        when(userRepository.findUserByUserId(any())).thenReturn(null);
        assertThrows(UserNotFoundException.class, ()->userService.addBankDetails(request));
    }

    @Test
    void testGetAllUserBankDetails_Success() {
        when(userBankDetailsRepository.findByUserEntityUserIdAndIsBankAccountDeletedFalse(any())).thenReturn(getListOfBankDetails(getUserEntity()));
        List<UserBankDetailsResponse> responseList = userService.getAllUserBankDetails("USER123");
        assertEquals(1, responseList.size());
    }

    @Test
    void testGetAllUserBankDetails_When_User_Not_Found() {
        when(userBankDetailsRepository.findByUserEntityUserIdAndIsBankAccountDeletedFalse(any())).thenReturn(null);
        assertThrows(UserBankAccountDetailsNotFoundException.class, ()->userService.getAllUserBankDetails("USER123"));
    }

    @Test
    void testDeleteBankDetails_Success() {
        when(userBankDetailsRepository.findByUserEntityUserIdAndUserBankId(any(), any())).thenReturn(getUserBankAccountEntity(getUserEntity()));
        when(paymentServiceHelper.sendHttpRequest(anyString(), any(), any(), anyString())).thenReturn(getFalseResponse());
        SuccessResponse successResponse = userService.deleteBankDetails("USER123", "UBA01");
        assertEquals(Constants.BANK_DETAILS_DELETED_SUCCESSFULLY, successResponse.getMessage());
    }

    @Test
    void testDeleteBankDetails_When_BankDetails_Not_Found() {
        when(userBankDetailsRepository.findByUserEntityUserIdAndUserBankId(any(), any())).thenReturn(null);
        assertThrows(UserBankAccountDetailsNotFoundException.class, ()->userService.deleteBankDetails("USER123", "UBA01"));
    }

    @Test
    void testDeleteBankDetails_When_DeactivateContact_Response_IsNull(){
        when(userBankDetailsRepository.findByUserEntityUserIdAndUserBankId(any(), any())).thenReturn(getUserBankAccountEntity(getUserEntity()));
        when(paymentServiceHelper.sendHttpRequest(anyString(), any(), any(), anyString())).thenReturn(null);
        assertThrows(RazorpayDataException.class, () -> userService.deleteBankDetails("USER123", "UBA01"));
    }

    @Test
    void testDeleteBankDetails_When_DeactivateFundAccount_Response_IsNull(){
        when(userBankDetailsRepository.findByUserEntityUserIdAndUserBankId(any(), any())).thenReturn(getUserBankAccountEntity(getUserEntity()));
        when(paymentServiceHelper.sendHttpRequest(anyString(), any(), any(), anyString()))
                .thenReturn(getFalseResponse())
                .thenReturn(null);
        assertThrows(RazorpayDataException.class, () -> userService.deleteBankDetails("USER123", "UBA01"));
    }

    private List<UserBankAccountEntity> getListOfBankDetails(UserEntity userEntity) {
        List<UserBankAccountEntity> userBankAccountEntities = new ArrayList<>();
        UserBankAccountEntity userBankAccountEntity = getUserBankAccountEntity(userEntity);
        userBankAccountEntities.add(userBankAccountEntity);
        return userBankAccountEntities;
    }

    private UserBankAccountEntity getNewUserBankAccountEntity() {
        UserBankAccountEntity userBankAccountEntity = new UserBankAccountEntity();
        userBankAccountEntity.setUserBankId("UBA01");
        userBankAccountEntity.setAccountHolderName("Harsh");
        userBankAccountEntity.setAccountNumber("1234567890");
        userBankAccountEntity.setBankName("SBI");
        userBankAccountEntity.setBankIfscCode("SBIN0001166");
        return userBankAccountEntity;
    }

    private UserBankAccountEntity getUserBankAccountEntity(UserEntity userEntity) {
        UserBankAccountEntity userBankAccountEntity = new UserBankAccountEntity();
        userBankAccountEntity.setUserBankId("UBA02");
        userBankAccountEntity.setAccountHolderName("Harshal");
        userBankAccountEntity.setAccountNumber("1234567890");
        userBankAccountEntity.setBankName("SBI");
        userBankAccountEntity.setBankIfscCode("SBIN0001166");
        userBankAccountEntity.setRazorpayFundAccountId("fund_8uebjhb2vsj");
        userBankAccountEntity.setRazorpayContactId("cont_uhbjh2hg3u7bvj");
        userBankAccountEntity.setIsBankAccountDeleted(false);
        userBankAccountEntity.setUserEntity(userEntity);
        return userBankAccountEntity;
    }

    private UserBankDetailsRequest getUserBankDetailsRequest() {
        UserBankDetailsRequest request = new UserBankDetailsRequest();
        request.setBankName("SBI");
        request.setAccountHolderName("Harsh");
        request.setAccountNumber("1234567890");
        request.setBankIfscCode("SBIN0001166");
        request.setUserId("UID01");
        return request;
    }

    private String getTrueResponse() {
        return "{\"id\":\"fa_Pc94t4yvlungyWD\",\"entity\":\"fund_account\",\"contact_id\":\"cont_Pc8mvlsdbehIrr\"," +
                "\"account_type\":\"bank_account\",\"bank_account\":{\"ifsc\":\"SBIN0001166\",\"bank_name\":\"State Bank of India\"," +
                "\"name\":\"Harsh G\",\"notes\":[],\"account_number\":\"1234567890\"},\"batch_id\":null,\"active\":true," +
                "\"created_at\":1735290459}";
    }

    private String getFalseResponse() {
        return "{\"id\":\"fa_Pc94t4yvlungyWD\",\"entity\":\"fund_account\",\"contact_id\":\"cont_Pc8mvlsdbehIrr\"," +
                "\"account_type\":\"bank_account\",\"bank_account\":{\"ifsc\":\"SBIN0001166\",\"bank_name\":\"State Bank of India\"," +
                "\"name\":\"Harsh G\",\"notes\":[],\"account_number\":\"1234567890\"},\"batch_id\":null,\"active\":false," +
                "\"created_at\":1735290459}";
    }

    private List<AddressEntity> getListAddressEntities() {
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

    private LoginRequest getLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhoneNumber("9999999999");
        loginRequest.setUserType("customer");
        return loginRequest;
    }

    private AdminEntity getAdminEntity() {
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

    private AffiliateUserEntity getAffiliateUserEntity() {
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

    private UserEntity getUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId("1");
        userEntity.setOrderEntities(new ArrayList<>());
        userEntity.setUserCreatedAt(LocalDateTime.now());
        userEntity.setReferredReferralCode("123456");
        userEntity.setReferralCode("123456");
        userEntity.setUserType("customer");
        userEntity.setAuthenticationSource("google");
        userEntity.setEmailAddressVerified(true);
        userEntity.setFirstName("jay");
        userEntity.setEmailAddress("jay@gmail.com");
        userEntity.setLastName("doe");
        userEntity.setPhoneNumber("1234567890");
        userEntity.setPhoneNumberVerified(true);
        userEntity.setAddressEntityList(List.of(new AddressEntity()));
        userEntity.setCardEntities(List.of(new CardEntity()));
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

    private CartDataEntity getCartDataEntity() {
        CartDataEntity cartData = new CartDataEntity();
        cartData.setTokenId("xyz");
        cartData.setSessionId("ss1");
        cartData.setDeviceId("did1");
        cartData.setTokenExpiry(1L);
        cartData.setCartDataItemEntities(getCartDataItemEntities());
        return cartData;
    }

    private List<CartItemEntity> getCartDataItemEntities() {
        List<CartItemEntity> cartItemEntities = new ArrayList<>();
        CartItemEntity cartItem1 = new CartItemEntity();
        cartItem1.setCartItemId("c1");
        cartItem1.setPrice(100.00);
        cartItem1.setProductImageUrl("p.png");
        cartItem1.setProductId("PID1111");
        cartItem1.setQuantity(1);
        cartItem1.setProductColor("Blue");
        cartItem1.setProductColorCode("#B1B1B1");
        cartItem1.setProductName("Pants");
        cartItem1.setProductOfferPercentage(5.0);
        cartItem1.setProductSize(30);
        cartItemEntities.add(cartItem1);
        return cartItemEntities;
    }
}