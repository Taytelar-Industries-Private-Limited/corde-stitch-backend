package com.cordestitch.service.serviceimplementation.user;

import com.cordestitch.entity.admin.AdminEntity;
import com.cordestitch.entity.affiliate.AffiliateUserEntity;
import com.cordestitch.entity.cart.CartDataEntity;
import com.cordestitch.entity.cart.CartItemEntity;
import com.cordestitch.entity.loyalty.LoyaltyPointsEntity;
import com.cordestitch.entity.otp.OTPEntity;
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
import com.cordestitch.request.cart.CartItemRequest;
import com.cordestitch.request.cart.CartRequest;
import com.cordestitch.request.user.AddressRequest;
import com.cordestitch.request.user.LoginRequest;
import com.cordestitch.request.user.UserBankDetailsRequest;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.token.AuthTokenResponse;
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.response.user.LoginResponse;
import com.cordestitch.response.user.UserBankDetailsResponse;
import com.cordestitch.service.service.payment.PaymentService;
import com.cordestitch.service.service.user.UserService;
import com.cordestitch.service.serviceimplementation.cart.CartDataServiceImplementation;
import com.cordestitch.service.serviceimplementation.cart.CartServiceImplementation;
import com.cordestitch.service.serviceimplementation.payment.PaymentServiceHelper;
import com.cordestitch.service.serviceimplementation.token.CookieService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import com.cordestitch.util.Generator;
import com.cordestitch.util.PaymentConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    private final AffiliateUserRepository affiliateUserRepository;

    private final OTPRepository otpRepository;

    private final AdminRepository adminRepository;

    private final AddressRepository addressRepository;

    private final Generator generator;

    private final ModelMapper modelMapper;

    private final CookieService cookieService;

    private final JwtServiceImplementation jwtServiceImplementation;

    private final PaymentService paymentService;

    private final UserBankDetailsRepository userBankDetailsRepository;

    private final PaymentServiceHelper paymentServiceHelper;

    private final CartDataServiceImplementation cartDataServiceImplementation;

    private final CartDataRepository cartDataRepository;

    private final CartServiceImplementation cartServiceImplementation;

    private static final String TOKEN_ID = "tokenId";
    private static final String ID = "id";
    private static final String IP = "ip";


    @Transactional
    @Override
    public LoginResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        log.info("Login request: {}", loginRequest);

        OTPEntity otpEntity = getOtpEntity(loginRequest.getPhoneNumber());
        log.info("OTP Entity Data: {}", otpEntity);

        if (!otpEntity.isOtpVerified()) {
            log.error(Constants.OTP_NOT_VERIFIED);
            throw new OtpNotFoundException(Constants.OTP_NOT_VERIFIED);
        }

        LoginResponse loginResponse;

        switch (loginRequest.getUserType()) {
            case Constants.CUSTOMER -> {
                UserEntity user = userRepository.findUserByPhoneNumber(loginRequest.getPhoneNumber());
                log.info("User Entity Data Response : {}", user);
                if (isNull(user)) {
                    log.info("Creating new UserEntity for CUSTOMER...");
                    user = createUserEntity(otpEntity, loginRequest);
                    userRepository.save(user);
                }
                loginResponse = handleCustomerLogin(user, response);
                mapToAddToCartFunction(request, response, user);
            }
            case Constants.AFFILIATE -> {
                AffiliateUserEntity affiliateUser = affiliateUserRepository.findUserByPhoneNumber(loginRequest.getPhoneNumber());
                log.info("AffiliateUser Entity Data Response : {}", affiliateUser);
                if (isNull(affiliateUser)) {
                    log.info("Creating new AffiliateUserEntity for AFFILIATE...");
                    affiliateUser = createAffiliateUserEntity(otpEntity, loginRequest);
                    affiliateUserRepository.save(affiliateUser);
                }
                loginResponse = handleAffiliateLogin(affiliateUser, response);
            }
            case Constants.ADMIN -> {
                AdminEntity admin = adminRepository.findUserByPhoneNumber(loginRequest.getPhoneNumber());
                log.info("Admin Entity Data Response : {}", admin);
                if (isNull(admin)) {
                    log.info("Creating new AdminEntity for ADMIN...");
                    admin = createAdminEntity(otpEntity, loginRequest);
                    adminRepository.save(admin);
                }
                loginResponse = handleAdminLogin(admin, response);
            }
            default -> {
                log.error(Constants.UNKNOWN_USER_TYPE);
                throw new UnknownUserTypeException(Constants.UNKNOWN_USER_TYPE);
            }
        }

        log.info("Login Response: {}", loginResponse);
        return loginResponse;
    }

    @Override
    public SuccessResponse addAddress(AddressRequest addressRequest) {
        log.info("Address Request: {}", addressRequest);
        UserEntity userEntity = userRepository.findUserByUserId(addressRequest.getUserId());
        validateUser(userEntity, addressRequest.getUserId());

        List<AddressEntity> addressEntities = addressRepository.findByUserEntityUserIdAndIsDeletedFalse(addressRequest.getUserId());

        if (addressEntities.size() >= 5) {
            log.error("Exception while adding address: {}", Constants.ADDRESS_LIMIT_EXCEEDED);
            throw new AddressLimitExceededException(Constants.ADDRESS_LIMIT_EXCEEDED);
        }

        if (checkAddressAlreadyExists(addressEntities, addressRequest)) {
            log.error("Exception while checking address : {}", Constants.ADDRESS_ALREADY_EXIST);
            throw new AddressAlreadyExistsException(Constants.ADDRESS_ALREADY_EXIST);
        }

        AddressEntity addressEntity = mapAddressRequestToEntity(new AddressEntity(), addressRequest, userEntity);
        addressRepository.save(addressEntity);
        log.info(Constants.ADDRESS_ADDED_SUCCESSFULLY + Constants.BRACKETS, addressEntity);

        return new SuccessResponse(Constants.ADDRESS_ADDED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    public List<AddressResponse> getAddresses(String userId) {
        log.info("Get addresses for user {}", userId);
        UserEntity userEntity = userRepository.findUserByUserId(userId);
        validateUser(userEntity, userId);

        List<AddressEntity> addressEntities = addressRepository.findByUserEntityUserIdAndIsDeletedFalse(userId);
        log.info("List Of Addresses : {}", addressEntities);

        return addressEntities.stream()
                .map(address -> modelMapper.map(address, AddressResponse.class))
                .toList();
    }

    @Override
    public SuccessResponse updateAddress(AddressRequest addressRequest) {
        log.info("Update Address: {}", addressRequest);
        UserEntity userEntity = userRepository.findUserByUserId(addressRequest.getUserId());
        validateUser(userEntity, addressRequest.getUserId());

        AddressEntity addressEntity = addressRepository.findByUserEntityUserIdAndAddressId(addressRequest.getUserId(), addressRequest.getAddressId());
        validateAddress(addressEntity, addressRequest.getAddressId());

        AddressEntity entity = mapAddressRequestToEntity(addressEntity, addressRequest, userEntity);
        addressRepository.save(entity);
        log.info(Constants.ADDRESS_UPDATED_SUCCESSFULLY + Constants.BRACKETS, entity);

        return new SuccessResponse(Constants.ADDRESS_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
    }


    @Override
    public SuccessResponse deleteAddress(String userId, String addressId) {
        log.info("Delete Address request user Id {}, Address Id {}", userId, addressId);
        UserEntity userEntity = userRepository.findUserByUserId(userId);
        validateUser(userEntity, userId);

        AddressEntity addressEntity = addressRepository.findByUserEntityUserIdAndAddressId(userId, addressId);
        validateAddress(addressEntity, addressId);

        addressEntity.setIsDeleted(true);
        addressRepository.save(addressEntity);
        log.info(Constants.ADDRESS_DELETED_SUCCESSFULLY + Constants.BRACKETS, addressEntity);

        return new SuccessResponse(Constants.ADDRESS_DELETED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    public SuccessResponse logout(HttpServletResponse response) {
        cookieService.clearExistingCookie(response);
        return new SuccessResponse(Constants.LOGOUT_SUCCESSFULLY, HttpStatus.OK.value());
    }

    @Override
    @Transactional(rollbackFor = {RazorpayDataException.class, IOException.class, InterruptedException.class, HttpTimeoutException.class, AccountNumberAlreadyExistsException.class})
    public SuccessResponse addBankDetails(UserBankDetailsRequest request) {
        log.info("Bank Details Request: {}", request);

        UserEntity userEntity = userRepository.findUserByUserId(request.getUserId());
        validateUser(userEntity, request.getUserId());

        List<UserBankAccountEntity> userBankAccountEntities = userBankDetailsRepository
                .findByUserEntityUserId(request.getUserId());
        log.info("List Of user bank accounts: {}", userBankAccountEntities);

        List<UserBankAccountEntity> activeAccounts = getActiveAccounts(userBankAccountEntities);
        log.info("List Of Active Accounts: {}", activeAccounts);

        UserBankAccountEntity existingAccount = findExistingDeletedAccount(userBankAccountEntities, request.getAccountNumber());
        log.info("Existing Account: {}", existingAccount);

        if (existingAccount != null) {
            return restoreDeletedAccount(existingAccount, activeAccounts, request);
        }

        validateNewAccount(activeAccounts, request.getAccountNumber());

        String razorpayContactId = getRazorpayContactId(activeAccounts, request, userEntity);
        String razorpayFundAccountId = paymentService.createFundAccount(razorpayContactId, request);
        log.info("Razorpay Fund Account Id : {}", razorpayFundAccountId);

        saveNewBankAccount(request, userEntity, razorpayContactId, razorpayFundAccountId);

        SuccessResponse successResponse = new SuccessResponse();
        successResponse.setMessage(Constants.BANK_DETAILS_ADDED_SUCCESSFULLY);
        successResponse.setStatusCode(HttpStatus.OK.value());
        log.info("Bank Details Response: {}", successResponse);
        return successResponse;
    }


    @Override
    public List<UserBankDetailsResponse> getAllUserBankDetails(String userId) {
        log.info("Get All Bank Details for user {}", userId);
        List<UserBankAccountEntity> entity = userBankDetailsRepository.findByUserEntityUserIdAndIsBankAccountDeletedFalse(userId);
        log.info("List Of Bank Details : {}", entity);

        if(isNull(entity)) {
            log.error(Constants.USER_BANK_DETAILS_NOT_FOUND + Constants.BRACKETS , userId);
            throw new UserBankAccountDetailsNotFoundException(Constants.USER_BANK_DETAILS_NOT_FOUND);
        }

        List<UserBankDetailsResponse> responseList = entity.stream()
                .map(userBankAccount -> modelMapper.map(userBankAccount, UserBankDetailsResponse.class))
                .toList();

        log.info("Get All Bank Details Response for the user: {}", responseList);
        return responseList;
    }


    @Transactional
    @Override
    public SuccessResponse deleteBankDetails(String userBankId, String userId) {
        log.info("Delete User Bank Details for user {} and bank id {}", userId, userBankId);
        UserBankAccountEntity entity = userBankDetailsRepository.findByUserEntityUserIdAndUserBankId(userId, userBankId);

        if (isNull(entity)) {
            log.error(Constants.USER_BANK_DETAILS_NOT_FOUND + Constants.BRACKETS, userId);
            throw new UserBankAccountDetailsNotFoundException(Constants.USER_BANK_DETAILS_NOT_FOUND);
        }

        String razorpayContactId = entity.getRazorpayContactId();
        String razorpayFundAccountId = entity.getRazorpayFundAccountId();
        log.info("Razorpay contact ID: {}, Razorpay Fund Account ID: {}", razorpayContactId, razorpayFundAccountId);

        try {
            SuccessResponse fundAccountResponse = activeOrDeactivateRazorpayFundAccount(razorpayFundAccountId, false);
            log.info("Deactivate Razorpay Fund Account response: {}", fundAccountResponse);

            if (fundAccountResponse.getStatusCode() == 200) {
                boolean hasOtherActiveAccounts = userBankDetailsRepository.existsByRazorpayContactIdAndIsBankAccountDeletedFalse(razorpayContactId);

                if (!hasOtherActiveAccounts) {
                    SuccessResponse contactResponse = activeOrDeactivateRazorpayContact(razorpayContactId, false);
                    log.info("Deactivate Razorpay Contact response: {}", contactResponse);

                    if (contactResponse.getStatusCode() != 200) {
                        log.error("Failed to deactivate Razorpay Contact. Status Code: {}", contactResponse.getStatusCode());
                        throw new RazorpayDataException(contactResponse.getMessage());
                    }
                }

                entity.setIsBankAccountDeleted(true);
                userBankDetailsRepository.save(entity);

                log.info(Constants.BANK_DETAILS_DELETED_SUCCESSFULLY + Constants.BRACKETS, userBankId);
                SuccessResponse response = new SuccessResponse();
                response.setMessage(Constants.BANK_DETAILS_DELETED_SUCCESSFULLY);
                response.setStatusCode(HttpStatus.OK.value());
                log.info("Delete User Bank Details Response: {}", response);
                return response;
            } else {
                log.error("Failed to deactivate Razorpay Fund Account. Status Code: {}", fundAccountResponse.getStatusCode());
                throw new RazorpayDataException(fundAccountResponse.getMessage());
            }
        } catch (Exception e) {
            log.error("Error deactivating Razorpay details for user {}: {}", userId, e.getMessage());
            throw new RazorpayDataException("Failed to deactivate Razorpay details");
        }
    }


    private LoginResponse handleCustomerLogin(UserEntity userEntity, HttpServletResponse response) {
        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setFirstName(userEntity.getFirstName());
        loginResponse.setLastName(userEntity.getLastName());
        loginResponse.setEmailAddress(userEntity.getEmailAddress());
        loginResponse.setEmailAddressVerified(userEntity.isEmailAddressVerified());
        loginResponse.setPhoneNumber(userEntity.getPhoneNumber());
        loginResponse.setReferralCode(userEntity.getReferralCode());
        loginResponse.setPhoneNumberVerified(userEntity.isPhoneNumberVerified());
        loginResponse.setUserType(userEntity.getUserType());
        loginResponse.setGender(userEntity.getGender());

        AuthTokenResponse authTokenResponse = generateToken(userEntity.getUserId(), userEntity.getUserType(),response);
        loginResponse.setAuthTokenResponse(authTokenResponse);
        return loginResponse;
    }


    private LoginResponse handleAffiliateLogin(AffiliateUserEntity affiliateUser, HttpServletResponse response) {
        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setFirstName(affiliateUser.getFirstName());
        loginResponse.setLastName(affiliateUser.getLastName());
        loginResponse.setEmailAddress(affiliateUser.getEmailAddress());
        loginResponse.setEmailAddressVerified(affiliateUser.isEmailAddressVerified());
        loginResponse.setPhoneNumber(affiliateUser.getPhoneNumber());
        loginResponse.setReferralCode(affiliateUser.getReferralCode());
        loginResponse.setPhoneNumberVerified(affiliateUser.isPhoneNumberVerified());
        loginResponse.setUserType(affiliateUser.getUserType());
        loginResponse.setGender(affiliateUser.getGender());

        AuthTokenResponse authTokenResponse = generateToken(affiliateUser.getAffiliateUserId(), affiliateUser.getUserType(), response);
        loginResponse.setAuthTokenResponse(authTokenResponse);
        return loginResponse;
    }

    private LoginResponse handleAdminLogin(AdminEntity adminEntity, HttpServletResponse response) {
        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setFirstName(adminEntity.getFirstName());
        loginResponse.setLastName(adminEntity.getLastName());
        loginResponse.setEmailAddress(adminEntity.getEmailAddress());
        loginResponse.setEmailAddressVerified(adminEntity.isEmailAddressVerified());
        loginResponse.setPhoneNumber(adminEntity.getPhoneNumber());
        loginResponse.setPhoneNumberVerified(adminEntity.isPhoneNumberVerified());
        loginResponse.setReferralCode(adminEntity.getReferralCode());
        loginResponse.setUserType(adminEntity.getUserType());
        loginResponse.setGender(adminEntity.getGender());

        AuthTokenResponse authTokenResponse = generateToken(adminEntity.getAdminId(), adminEntity.getUserType(), response);
        loginResponse.setAuthTokenResponse(authTokenResponse);
        return loginResponse;
    }

    private OTPEntity getOtpEntity(String phoneNumber) {
        OTPEntity otpEntity = otpRepository.findByPhoneNumber(phoneNumber);
        if (isNull(otpEntity)) {
            throw new OtpNotFoundException(Constants.OTP_ENTITY_NOT_FOUND);
        }
        return otpEntity;
    }

    private UserEntity createUserEntity(OTPEntity otpEntity, LoginRequest loginRequest) {
        UserEntity user = new UserEntity();
        user.setUserId(generator.generateId(Constants.USER_ID));
        user.setEmailAddressVerified(otpEntity.isEmailAddressVerified());
        user.setPhoneNumber(otpEntity.getPhoneNumber());
        user.setPhoneNumberVerified(otpEntity.isOtpVerified());
        user.setReferralCode(generator.referralCode());
        user.setUserType(loginRequest.getUserType());
        user.setAuthenticationSource(generator.createAuthenticationSource(loginRequest.getUserType()));
        user.setUserCreatedAt(LocalDateTime.now(ZoneId.of(Constants.ZONE)));

        LoyaltyPointsEntity loyaltyPoints = new LoyaltyPointsEntity();
        loyaltyPoints.setLoyaltyId(generator.generateId(Constants.LOYALTY_ID));
        loyaltyPoints.setTotalLoyaltyPoints(0.0);
        loyaltyPoints.setTotalExpiredPoints(0.0);
        loyaltyPoints.setTotalRedeemedPoints(0.0);
        loyaltyPoints.setLastUpdated(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        loyaltyPoints.setUserEntity(user);

        user.setLoyaltyPointsEntity(loyaltyPoints);
        return user;
    }

    private AffiliateUserEntity createAffiliateUserEntity(OTPEntity otpEntity, LoginRequest loginRequest) {
        AffiliateUserEntity affiliateUserEntity = new AffiliateUserEntity();
        affiliateUserEntity.setAffiliateUserId(generator.generateId(Constants.AFFILIATE_USER_ID));
        affiliateUserEntity.setEmailAddressVerified(otpEntity.isEmailAddressVerified());
        affiliateUserEntity.setPhoneNumber(otpEntity.getPhoneNumber());
        affiliateUserEntity.setPhoneNumberVerified(otpEntity.isOtpVerified());
        affiliateUserEntity.setReferralCode(generator.referralCode());
        affiliateUserEntity.setUserType(loginRequest.getUserType());
        affiliateUserEntity.setAuthenticationSource(generator.createAuthenticationSource(loginRequest.getUserType()));
        affiliateUserEntity.setUserCreatedAt(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        return affiliateUserEntity;
    }

    private AdminEntity createAdminEntity(OTPEntity otpEntity, LoginRequest loginRequest) {
        AdminEntity admin = new AdminEntity();
        admin.setAdminId(generator.generateId(Constants.ADMIN_ID));
        admin.setEmailAddressVerified(otpEntity.isEmailAddressVerified());
        admin.setPhoneNumber(otpEntity.getPhoneNumber());
        admin.setPhoneNumberVerified(otpEntity.isOtpVerified());
        admin.setUserType(loginRequest.getUserType());
        admin.setAuthenticationSource(generator.createAuthenticationSource(loginRequest.getUserType()));
        admin.setUserCreatedAt(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
        admin.setReferralCode(generator.referralCode());
        return admin;
    }

    private boolean checkAddressAlreadyExists(List<AddressEntity> addressEntities, AddressRequest addressRequest) {
        return addressEntities.stream()
                .anyMatch(address ->
                        address.getFirstName().equalsIgnoreCase(addressRequest.getFirstName()) &&
                                address.getLastName().equalsIgnoreCase(addressRequest.getLastName()) &&
                                address.getPhoneNumber().equalsIgnoreCase(addressRequest.getPhoneNumber()) &&
                                address.getBuildingName().equalsIgnoreCase(addressRequest.getBuildingName()) &&
                                address.getStreetName().equalsIgnoreCase(addressRequest.getStreetName()) &&
                                address.getCityName().equalsIgnoreCase(addressRequest.getCityName()) &&
                                address.getStateName().equalsIgnoreCase(addressRequest.getStateName()) &&
                                address.getCountryName().equalsIgnoreCase(addressRequest.getCountryName()) &&
                                address.getPinCode().equalsIgnoreCase(addressRequest.getPinCode()) &&
                                address.getTypeOfAddress().equalsIgnoreCase(addressRequest.getTypeOfAddress()) &&
                                address.getLandMark() != null && address.getLandMark().equalsIgnoreCase(addressRequest.getLandMark() != null ? addressRequest.getLandMark() : ""));
    }

    private AddressEntity mapAddressRequestToEntity(AddressEntity addressEntity, AddressRequest addressRequest, UserEntity userEntity) {
        addressEntity.setAddressId(
                (addressRequest.getAddressId() == null || addressRequest.getAddressId().isEmpty())
                        ? generator.generateId(Constants.ADDRESS_ID)
                        : addressRequest.getAddressId()
        );
        addressEntity.setFirstName(addressRequest.getFirstName());
        addressEntity.setLastName(addressRequest.getLastName());
        addressEntity.setPhoneNumber(addressRequest.getPhoneNumber());
        addressEntity.setBuildingName(addressRequest.getBuildingName());
        addressEntity.setStreetName(addressRequest.getStreetName());
        addressEntity.setCityName(addressRequest.getCityName());
        addressEntity.setStateName(addressRequest.getStateName());
        addressEntity.setCountryName(addressRequest.getCountryName());
        addressEntity.setPinCode(addressRequest.getPinCode());
        addressEntity.setTypeOfAddress(addressRequest.getTypeOfAddress());
        addressEntity.setLandMark(addressRequest.getLandMark());
        addressEntity.setAltPhone(addressRequest.getAltPhone());
        addressEntity.setUserEntity(userEntity);
        return addressEntity;
    }


    private void validateUser(UserEntity userEntity, String userId) {
        if (isNull(userEntity)) {
            log.info("Exception : {} {}", Constants.USER_NOT_FOUND, userId);
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }
    }

    private void validateAddress(AddressEntity addressEntity, String addressId) {
        if (isNull(addressEntity)) {
            log.info("Exception : {} {}", Constants.ADDRESS_NOT_FOUND, addressId);
            throw new AddressNotFoundException(Constants.ADDRESS_NOT_FOUND);
        }
    }

    private AuthTokenResponse generateToken(String userId, String userType, HttpServletResponse response) {
        String jwtToken = jwtServiceImplementation.generateToken(userId, userType);
        String refreshToken = jwtServiceImplementation.generateRefreshToken(userId, userType);
        String tokenExpiryTime = jwtServiceImplementation.extractTokenExpiryTime(jwtToken);
        String refreshTokenExpiryTime = jwtServiceImplementation.extractTokenExpiryTime(refreshToken);
        log.info("JWT token: {}", jwtToken);
        log.info("Refresh token: {}", refreshToken);
        log.info("Token Expiry time: {}", tokenExpiryTime);
        log.info("Refresh Token Expiry time: {}", refreshTokenExpiryTime);

        cookieService.clearExistingCookie(response);
        cookieService.createJwtCookie(jwtToken, response);
        cookieService.createRefreshCookie(refreshToken, response);

        return new AuthTokenResponse(jwtToken, tokenExpiryTime, refreshTokenExpiryTime, HttpStatus.OK.value());
    }

    private List<UserBankAccountEntity> getActiveAccounts(List<UserBankAccountEntity> userBankAccountEntities) {
        return userBankAccountEntities.stream()
                .filter(account -> !account.getIsBankAccountDeleted())
                .toList();
    }

    private UserBankAccountEntity findExistingDeletedAccount(List<UserBankAccountEntity> userBankAccountEntities, String accountNumber) {
        return userBankAccountEntities.stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber) && account.getIsBankAccountDeleted())
                .findFirst()
                .orElse(null);
    }

    private SuccessResponse restoreDeletedAccount(UserBankAccountEntity existingAccount, List<UserBankAccountEntity> activeAccounts, UserBankDetailsRequest request) {
        log.info("Restoring previously deleted bank account: {}", request.getAccountNumber());

        if (activeAccounts.size() >= 3) {
            log.error("Cannot restore bank account: {}", Constants.MAX_BANK_ACCOUNTS_REACHED);
            throw new RazorpayDataException(Constants.MAX_BANK_ACCOUNTS_REACHED);
        }

        try {
            activateRazorpayDetails(existingAccount);
            updateAndSaveExistingAccount(existingAccount, request);

            SuccessResponse successResponse = new SuccessResponse();
            successResponse.setMessage(Constants.BANK_DETAILS_RESTORED_SUCCESSFULLY);
            successResponse.setStatusCode(HttpStatus.OK.value());
            log.info("Bank Details Response: {}", successResponse);
            return successResponse;
        } catch (Exception e) {
            log.error("Error restoring Razorpay details for user {}: {}", request.getUserId(), e.getMessage());
            throw new RazorpayDataException("Failed to restore Razorpay details");
        }
    }

    private void validateNewAccount(List<UserBankAccountEntity> activeAccounts, String accountNumber) {
        if (activeAccounts.stream().anyMatch(account -> account.getAccountNumber().equals(accountNumber))) {
            log.error("Exception while checking account number : {}", Constants.ACCOUNT_NUMBER_ALREADY_EXIST);
            throw new AccountNumberAlreadyExistsException(Constants.ACCOUNT_NUMBER_ALREADY_EXIST);
        }

        if (activeAccounts.size() >= 3) {
            log.error("Exception while adding bank details: {}", Constants.MAX_BANK_ACCOUNTS_REACHED);
            throw new RazorpayDataException(Constants.MAX_BANK_ACCOUNTS_REACHED);
        }
    }

    private String getRazorpayContactId(List<UserBankAccountEntity> activeAccounts, UserBankDetailsRequest request, UserEntity userEntity) {
        return activeAccounts.isEmpty()
                ? paymentService.createRazorpayContact(request.getAccountHolderName(), userEntity.getEmailAddress(), userEntity.getPhoneNumber())
                : activeAccounts.getFirst().getRazorpayContactId();
    }

    private void activateRazorpayDetails(UserBankAccountEntity existingAccount) {
        SuccessResponse contactResponse = activeOrDeactivateRazorpayContact(existingAccount.getRazorpayContactId(), true);
        if (contactResponse.getStatusCode() != 200) {
            log.error("Failed to activate Razorpay Contact. Status Code: {}", contactResponse.getStatusCode());
            throw new RazorpayDataException(contactResponse.getMessage());
        }

        SuccessResponse fundResponse = activeOrDeactivateRazorpayFundAccount(existingAccount.getRazorpayFundAccountId(), true);
        if (fundResponse.getStatusCode() != 200) {
            log.error("Failed to activate Razorpay Fund Account. Status Code: {}", fundResponse.getStatusCode());
            throw new RazorpayDataException(fundResponse.getMessage());
        }
    }

    private void updateAndSaveExistingAccount(UserBankAccountEntity existingAccount, UserBankDetailsRequest request) {
        existingAccount.setIsBankAccountDeleted(false);
        existingAccount.setBankName(request.getBankName());
        existingAccount.setAccountHolderName(request.getAccountHolderName());
        existingAccount.setBankIfscCode(request.getBankIfscCode());
        userBankDetailsRepository.save(existingAccount);
        log.info("Bank account with accountNumber {} has been restored", request.getAccountNumber());
    }

    private void saveNewBankAccount(UserBankDetailsRequest request, UserEntity userEntity, String razorpayContactId, String razorpayFundAccountId) {
        UserBankAccountEntity entity = modelMapper.map(request, UserBankAccountEntity.class);
        entity.setUserBankId(generator.generateId(Constants.USER_BANK_ID));
        entity.setUserEntity(userEntity);
        entity.setRazorpayContactId(razorpayContactId);
        entity.setRazorpayFundAccountId(razorpayFundAccountId);
        userBankDetailsRepository.save(entity);
        log.info(Constants.BANK_DETAILS_ADDED_SUCCESSFULLY + Constants.BRACKETS, entity);
    }

    private SuccessResponse activeOrDeactivateRazorpayContact(String razorpayContactId, boolean value){
        String url = PaymentConstants.CONTACT_ACTIVE_DEACTIVATE_URL + razorpayContactId;

        String errorMessage = value ? PaymentConstants.ACTIVATE_CONTACT_ERR_MSG : PaymentConstants.DEACTIVATE_CONTACT_ERR_MSG;

        JSONObject payload = new JSONObject();
        payload.put(PaymentConstants.ACTIVE, value);
        log.info("Payload for activating/deactivating contact: {}", payload);

        String responseBody = paymentServiceHelper.sendHttpRequest(url, payload, HttpMethod.PATCH,errorMessage);

        if (responseBody != null && (responseBody.contains("\"active\":true") || responseBody.contains("\"active\":false"))) {
            if(value){
                log.info("Contact activated successfully: {}", razorpayContactId);
                return new SuccessResponse(Constants.CONTACT_ACTIVATED_SUCCESS, HttpStatus.OK.value());
            }
            log.info("Contact deactivated successfully: {}", razorpayContactId);
            return new SuccessResponse(Constants.CONTACT_DEACTIVATED_SUCCESS, HttpStatus.OK.value());
        } else {
            String message = value ? PaymentConstants.CONTACT_ACTIVATED_FAILED : PaymentConstants.CONTACT_DEACTIVATED_FAILED;
            log.error("Failed to deactivate Razorpay contact. Response: {}", responseBody);
            return new SuccessResponse(message,HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private SuccessResponse activeOrDeactivateRazorpayFundAccount(String razorpayFundAccountId, boolean value) {
        String url = PaymentConstants.FUND_ACCOUNT_ACTIVE_DEACTIVATE_URL + razorpayFundAccountId;

        String errorMessage = value ? PaymentConstants.ACTIVATE_FUND_ACCOUNT_ERR_MSG : PaymentConstants.DEACTIVATE_FUND_ACCOUNT_ERR_MSG;

        JSONObject payload = new JSONObject();
        payload.put(PaymentConstants.ACTIVE, value);

        String responseBody = paymentServiceHelper.sendHttpRequest(url, payload, HttpMethod.PATCH,errorMessage);

        if (responseBody != null && (responseBody.contains("\"active\":true") || responseBody.contains("\"active\":false"))) {
            if(value) {
                log.info("Fund account activated successfully: {}", razorpayFundAccountId);
                return new SuccessResponse(Constants.FUND_ACCOUNT_ACTIVATED_SUCCESS, HttpStatus.OK.value());
            }
            log.info("Fund account deactivated successfully: {}", razorpayFundAccountId);
            return new SuccessResponse(Constants.FUND_ACCOUNT_DEACTIVATED_SUCCESS, HttpStatus.OK.value());
        } else {
            String message = value? PaymentConstants.FUND_ACCOUNT_ACTIVATION_FAILED : PaymentConstants.FUND_ACCOUNT_DEACTIVATION_FAILED;
            log.error("Failed to deactivate Razorpay fund account. Response: {}", responseBody);
            return new SuccessResponse(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void mapToAddToCartFunction(HttpServletRequest request, HttpServletResponse response, UserEntity user) {
        Map<String, String> cookieValues = cartDataServiceImplementation.extractCookieValues(request);
        String tokenId = cookieValues.get(TOKEN_ID);
        String sessionId = cookieValues.get(ID);
        String deviceId = cookieValues.get(IP);

        cartDataServiceImplementation.checkSessionValid(tokenId, sessionId, deviceId, request);

        CartDataEntity cartDataEntity = cartDataRepository.findByDeviceIdAndTokenId(deviceId, tokenId);
        if(!isNull(cartDataEntity)) {
            List<CartItemEntity> cartItemEntities = cartDataEntity.getCartDataItemEntities();

            List<CartItemRequest> cartItemRequests = mapToCartItemRequests(cartItemEntities);
            CartRequest cartRequest = new CartRequest();
            cartRequest.setUserId(user.getUserId());
            cartRequest.setCartItemRequests(cartItemRequests);

            SuccessResponse successResponse = cartServiceImplementation.addToCart(cartRequest);
            if (successResponse.getStatusCode() == 200) {
                cartDataRepository.delete(cartDataEntity);
                cookieService.clearCookies(response);
            }
        }
    }

    private List<CartItemRequest> mapToCartItemRequests(List<CartItemEntity> cartItemEntities) {
        List<CartItemRequest> result = new ArrayList<>();
        cartItemEntities.forEach(cartItemEntity -> {
            CartItemRequest cartItemRequest = new CartItemRequest();
            cartItemRequest.setProductId(cartItemEntity.getProductId());
            cartItemRequest.setProductName(cartItemEntity.getProductName());
            cartItemRequest.setProductColor(cartItemEntity.getProductColor());
            cartItemRequest.setProductColorCode(cartItemEntity.getProductColorCode());
            cartItemRequest.setProductSize(cartItemEntity.getProductSize());
            cartItemRequest.setPrice(cartItemEntity.getPrice());
            cartItemRequest.setQuantity(cartItemEntity.getQuantity());
            cartItemRequest.setProductOfferPercentage(cartItemEntity.getProductOfferPercentage());
            result.add(cartItemRequest);
        });
        return result;
    }
}
