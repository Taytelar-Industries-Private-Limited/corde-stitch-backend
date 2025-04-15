package com.cordestitch.service.serviceimplementation.user;

import com.cordestitch.entity.admin.AdminEntity;
import com.cordestitch.entity.affiliate.AffiliateUserEntity;
import com.cordestitch.entity.otp.OTPEntity;
import com.cordestitch.entity.user.AddressEntity;
import com.cordestitch.entity.user.UserEntity;
import com.cordestitch.exception.affiliate.FailedToSendOtpException;
import com.cordestitch.exception.otp.OtpNotFoundException;
import com.cordestitch.exception.user.DataCheckReflectionException;
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
import com.cordestitch.response.user.AddressResponse;
import com.cordestitch.response.user.UserDetailsResponse;
import com.cordestitch.service.service.user.AuthRegisterService;
import com.cordestitch.service.serviceimplementation.otp.OTPServiceImplementation;
import com.cordestitch.util.Constants;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthRegisterServiceImplementation implements AuthRegisterService {

    private final UserRepository userRepository;

    private final AffiliateUserRepository affiliateUserRepository;

    private final AdminRepository adminRepository;

    private final OTPRepository otpRepository;

    private final AddressRepository addressRepository;

    private final JavaMailSender javaMailSender;

    private final OTPServiceImplementation otpServiceImplementation;
    private final Random random = new Random();
    private static final ModelMapper modelMapper = new ModelMapper();

    private final UserServiceImplementation serviceImplementation;

    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    @Transactional
    @Override
    public SuccessResponse updateEmailAddressOrPhoneNumber(UpdateRequest request) {
        log.info("Update Email Address or Phone Number Request: {}", request);
        SuccessResponse successResponse = new SuccessResponse();
        if (request.getEmailAddress() != null) {
            successResponse = sendToEmailAddress(request);
        } else if (request.getPhoneNumber() != null) {
            successResponse = sendToPhoneNumber(request);
        }
        return successResponse;
    }

    @Transactional
    @Override
    public SuccessResponse validateOTPForEmailOrPhoneNumber(ValidateRequest request) {
        log.info("Validate OTP Request: {}", request);
        SuccessResponse successResponse = new SuccessResponse();
        if (request.getEmailAddress() != null) {
            OTPEntity otpEntity = getOtpEntityByEmail(request.getEmailAddress());
            log.info("OTP Entity: {}", otpEntity);
            successResponse = verifyOTP(otpEntity, request);
        } else if (request.getPhoneNumber() != null) {
            OTPEntity otpEntity = getOtpEntityByPhoneNumber(request.getPhoneNumber());
            log.info("OTP Entity: {}", otpEntity);
            successResponse = verifyOTP(otpEntity, request);
        }
        return successResponse;
    }

    @Override
    public UpdateDataResponse checkEmailOrPhoneNumberVerified(String userId) {
        log.info("Checking email or phone number is verified : {}", userId);

        UpdateDataResponse response = new UpdateDataResponse();
        if (userId.contains(Constants.USER_ID)) {
            response = checkInUserEntity(userId);
        } else if (userId.contains(Constants.AFFILIATE_USER_ID)) {
            response = checkInAffiliateEntity(userId);
        } else if (userId.contains(Constants.ADMIN_ID)) {
            response = checkInAdminEntity(userId);
        }
        return response;
    }

    @Override
    public SuccessResponse updateProfile(UpdateProfileRequest request) {
        log.info("Update Profile Request : {}", request);

        SuccessResponse response = new SuccessResponse();

        if (request.getUserId().contains(Constants.USER_ID)) {
            response = updateProfileForUserEntity(request);
        } else if (request.getUserId().contains(Constants.ADMIN_ID)) {
            response = updateProfileForAdminEntity(request);
        } else if (request.getUserId().contains(Constants.AFFILIATE_USER_ID)) {
            response = updateProfileForAffiliateUserEntity(request);
        }
        return response;
    }

    @Override
    public UserDetailsResponse getUserDetails(String userId) {
        log.info("Getting user details for the userId : {}", userId);
        UserEntity userEntity = userRepository.findUserByUserId(userId);
        if (isNull(userEntity)) {
            log.info(Constants.USER_NOT_FOUND);
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        userDetailsResponse.setFirstName(userEntity.getFirstName());
        userDetailsResponse.setLastName(userEntity.getLastName());
        userDetailsResponse.setGender(userEntity.getGender());
        userDetailsResponse.setPhoneNumber(userEntity.getPhoneNumber());
        userDetailsResponse.setPhoneNumberVerified(userEntity.isPhoneNumberVerified());
        userDetailsResponse.setEmailAddress(userEntity.getEmailAddress());
        userDetailsResponse.setEmailAddressVerified(userEntity.isEmailAddressVerified());
        userDetailsResponse.setUserType(userEntity.getUserType());
        userDetailsResponse.setReferralCode(userEntity.getReferralCode());

        List<AddressEntity> addressEntities = addressRepository.findByUserEntityUserIdAndIsDeletedFalse(userId);
        log.info("List of Address : {}", addressEntities);
        List<AddressResponse> addressResponses = modelMapper.map(addressEntities, new TypeToken<List<AddressResponse>>() {
        }.getType());
        userDetailsResponse.setAddressResponses(addressResponses);
        log.info("UserDetails Response : {}", userDetailsResponse);
        return userDetailsResponse;
    }

    @Transactional
    @Override
    public SuccessResponse updateUserDetails(UpdateUserDetailsRequest request) {
        log.info("Update User Details Request : {}", request);

        UserEntity userEntity = userRepository.findUserByUserId(request.getUserId());
        if (isNull(userEntity)) {
            log.info(Constants.USER_NOT_FOUND);
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        userEntity.setFirstName(request.getFirstName() != null ? request.getFirstName() : userEntity.getFirstName());
        userEntity.setLastName(request.getLastName() != null ? request.getLastName() : userEntity.getLastName());
        userEntity.setGender(request.getGender() != null ? request.getGender() : userEntity.getGender());
        userEntity.setEmailAddress(request.getEmailAddress() != null ? request.getEmailAddress() : userEntity.getEmailAddress());
        userEntity.setEmailAddressVerified(request.getEmailAddress() != null);
        userRepository.save(userEntity);

        saveEmailToOTPEntity(request.getEmailAddress(), userEntity.getPhoneNumber());

        if (request.getAddressRequests() != null && !request.getAddressRequests().isEmpty()) {
            for (AddressRequest addressRequest : request.getAddressRequests()) {
                addressRequest.setUserId(request.getUserId());
                SuccessResponse successResponse = serviceImplementation.addAddress(addressRequest);
                log.info("UpdateUserDetails::AddAddressResponse: {}", successResponse);
            }
        }

        return new SuccessResponse(Constants.USER_DETAILS_UPDATED_SUCCESSFULLY, HttpStatus.OK.value());
    }

    private void saveEmailToOTPEntity(String emailAddress, String phoneNumber) {
        log.info("Email Address: {}, Phone Number: {}", emailAddress, phoneNumber);
        OTPEntity otpEntity = otpRepository.findByPhoneNumber(phoneNumber);
        if (isNull(otpEntity)) {
            log.error(Constants.OTP_ENTITY_NOT_FOUND + " :{}", phoneNumber);
            throw new OtpNotFoundException(Constants.OTP_ENTITY_NOT_FOUND);
        }
        otpEntity.setEmailAddress(emailAddress);
        otpEntity.setEmailAddressVerified(true);
        otpRepository.save(otpEntity);
    }

    private UpdateDataResponse batchTheUpdateDataResponse(String emailAddress, boolean emailAddressVerified, String phoneNumber, boolean phoneNumberVerified) {
        UpdateDataResponse response = new UpdateDataResponse();
        response.setEmailAddress(emailAddress);
        response.setEmailAddressVerified(emailAddressVerified);
        response.setPhoneNumber(phoneNumber);
        response.setPhoneNumberVerified(phoneNumberVerified);
        log.info("Update Data Response : {}", response);
        return response;
    }

    private OTPEntity getOtpEntityByPhoneNumber(String phoneNumber) {
        OTPEntity otpEntity = otpRepository.findByPhoneNumber(phoneNumber);

        if (isNull(otpEntity)) {
            log.error(Constants.PHONE_NUMBER_MISMATCH);
            throw new UserDetailsMissMatchException(Constants.PHONE_NUMBER_MISMATCH);
        }
        return otpEntity;
    }

    private OTPEntity getOtpEntityByEmail(String emailAddress) {
        return otpRepository.findByEmailAddress(emailAddress).orElseThrow(() -> {
            log.error(Constants.EMAIL_MISMATCH);
            return new UserDetailsMissMatchException(Constants.EMAIL_MISMATCH);
        });
    }

    private SuccessResponse verifyOTP(OTPEntity otpEntity, ValidateRequest request) {
        if (request.getEmailAddress() != null) {
            if (isEmailOtpValid(otpEntity, request.getOtpCode())) {
                updateEmailOrPhoneNumberInEntity(request);
                markOtpAsVerified(otpEntity, request);
                return new SuccessResponse(Constants.OTP_VERIFIED_SUCCESSFULLY, HttpStatus.OK.value());
            } else {
                log.error(Constants.OTP_MISMATCH + " :{}", request.getOtpCode());
                throw new UserDetailsMissMatchException(Constants.OTP_MISMATCH);
            }
        } else if (request.getPhoneNumber() != null) {
            if (isPhoneNumberValid(otpEntity, request.getOtpCode())) {
                updateEmailOrPhoneNumberInEntity(request);
                markOtpAsVerified(otpEntity, request);
                return new SuccessResponse(Constants.OTP_VERIFIED_SUCCESSFULLY, HttpStatus.OK.value());

            } else {
                log.error(Constants.OTP_MISMATCH + " :{}", request.getOtpCode());
                throw new UserDetailsMissMatchException(Constants.OTP_MISMATCH);
            }
        }
        log.error("No email address or phone number provided for OTP verification.");
        throw new UserDetailsMissMatchException(Constants.REQUEST_DATA_MISSED);
    }

    private boolean isPhoneNumberValid(OTPEntity otpEntity, String otpCode) {
        return otpEntity.getOtpCode().equals(otpCode);
    }

    private boolean isEmailOtpValid(OTPEntity otpEntity, String otpCode) {
        return otpEntity.getEmailAddressOtpCode().equals(otpCode);
    }

    private void markOtpAsVerified(OTPEntity otpEntity, ValidateRequest request) {
        if (request.getEmailAddress() != null) {
            otpEntity.setEmailAddressVerified(true);
            otpRepository.save(otpEntity);
        } else if (request.getPhoneNumber() != null) {
            otpEntity.setOtpVerified(true);
            otpRepository.save(otpEntity);
        }
    }

    private void updateEmailOrPhoneNumberInEntity(ValidateRequest request) {
        if (request.getUserId() != null) {
            if (request.getUserId().contains(Constants.USER_ID)) {
                UserEntity userEntity = findUserEntity(request.getUserId());
                validateUserEntity(userEntity, request.getUserId());
                updateEmailOrPhoneForUser(userEntity, request);
            } else if (request.getUserId().contains(Constants.AFFILIATE_USER_ID)) {
                AffiliateUserEntity affiliateUserEntity = getAffiliateUserEntity(request.getUserId());
                updateEmailOrPhoneForAffiliateUser(affiliateUserEntity, request);
            } else if (request.getUserId().contains(Constants.ADMIN_ID)) {
                AdminEntity adminEntity = getAdminEntity(request.getUserId());
                updateEmailOrPhoneForAdmin(adminEntity, request);
            }
        }
    }


    private void validateUserEntity(Object userEntity, String userId) {
        if (isNull(userEntity)) {
            log.error(Constants.USER_NOT_FOUND + " :{}", userId);
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }
    }

    private void updateEmailOrPhoneForUser(UserEntity userEntity, ValidateRequest request) {
        if (request.getEmailAddress() != null) {
            userEntity.setEmailAddress(request.getEmailAddress());
            userEntity.setEmailAddressVerified(true);
        } else if (request.getPhoneNumber() != null) {
            userEntity.setPhoneNumber(request.getPhoneNumber());
            userEntity.setPhoneNumberVerified(true);
        }
        userRepository.save(userEntity);
    }

    private void updateEmailOrPhoneForAffiliateUser(AffiliateUserEntity affiliateUserEntity, ValidateRequest request) {
        if (request.getEmailAddress() != null) {
            affiliateUserEntity.setEmailAddress(request.getEmailAddress());
            affiliateUserEntity.setEmailAddressVerified(true);
        } else if (request.getPhoneNumber() != null) {
            affiliateUserEntity.setPhoneNumber(request.getPhoneNumber());
            affiliateUserEntity.setPhoneNumberVerified(true);
        }
        affiliateUserRepository.save(affiliateUserEntity);
    }

    private void updateEmailOrPhoneForAdmin(AdminEntity adminEntity, ValidateRequest request) {
        if (request.getEmailAddress() != null) {
            adminEntity.setEmailAddress(request.getEmailAddress());
            adminEntity.setEmailAddressVerified(true);
        } else if (request.getPhoneNumber() != null) {
            adminEntity.setPhoneNumber(request.getPhoneNumber());
            adminEntity.setPhoneNumberVerified(true);
        }
        adminRepository.save(adminEntity);
    }

    private SuccessResponse sendToEmailAddress(UpdateRequest request) {
        log.info("Sending OTP to email address: {}", request.getEmailAddress());

        UpdateDataResponse response = checkEntity(request);
        if (response == null) {
            log.error("User not found with email address: {}", request.getEmailAddress());
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        checkUpdatingData(response, request);
        return handleOtpForEmail(response, request.getEmailAddress());
    }

    private void checkUpdatingData(UpdateDataResponse response, UpdateRequest request) {
        if (request.getEmailAddress() != null && request.getEmailAddress().equals(response.getEmailAddress())) {
            log.error("Attempt to update with the same email: {}", request.getEmailAddress());
            throw new UserDetailsMissMatchException(Constants.SAME_EMAIL_ERROR_MSG);
        }

        if (request.getPhoneNumber() != null && request.getPhoneNumber().equals(response.getPhoneNumber())) {
            log.error("Attempt to update with the same phone number: {}", request.getPhoneNumber());
            throw new UserDetailsMissMatchException(Constants.SAME_PHONE_NUMBER_ERROR_MSG);
        }
    }

    private UpdateDataResponse checkEntity(UpdateRequest request) {
        if (request.getUserId() != null) {
            if (request.getUserId().contains(Constants.USER_ID)) {
                UserEntity userEntity = userRepository.findUserByUserId(request.getUserId());
                validateUserEntity(userEntity, request.getUserId());
                return buildUpdateDataResponse(userEntity.getEmailAddress(), userEntity.getPhoneNumber());
            } else if (request.getUserId().contains(Constants.AFFILIATE_USER_ID)) {
                AffiliateUserEntity affiliateUser = getAffiliateUserEntity(request.getUserId());
                return buildUpdateDataResponse(affiliateUser.getEmailAddress(), affiliateUser.getPhoneNumber());
            } else if (request.getUserId().contains(Constants.ADMIN_ID)) {
                AdminEntity adminEntity = getAdminEntity(request.getUserId());
                return buildUpdateDataResponse(adminEntity.getEmailAddress(), adminEntity.getPhoneNumber());
            }
        }
        return null;
    }

    private AffiliateUserEntity getAffiliateUserEntity(String userId) {
        return affiliateUserRepository.findByAffiliateUserId(userId).orElseThrow(() -> {
            log.error(Constants.USER_NOT_FOUND);
            return new UserNotFoundException(Constants.USER_NOT_FOUND);
        });
    }

    private AdminEntity getAdminEntity(String userId) {
        return adminRepository.findByAdminId(userId).orElseThrow(() -> {
            log.error(Constants.USER_NOT_FOUND);
            return new UserNotFoundException(Constants.USER_NOT_FOUND);
        });
    }

    private UpdateDataResponse buildUpdateDataResponse(String emailAddress, String phoneNumber) {
        UpdateDataResponse response = new UpdateDataResponse();
        response.setEmailAddress(emailAddress);
        response.setPhoneNumber(phoneNumber);
        return response;
    }


    private SuccessResponse handleOtpForEmail(UpdateDataResponse response, String emailAddress) {
        log.info("Handling OTP for email: {}", emailAddress);

        Optional<OTPEntity> otpEntityOptional = checkOtpEntity(response.getEmailAddress());
        if (otpEntityOptional.isEmpty()) {
            log.error(Constants.OTP_ENTITY_DATA_NOT_FOUND + " :{}", response.getEmailAddress());
            throw new OtpNotFoundException(Constants.OTP_ENTITY_DATA_NOT_FOUND);
        }
        return sendOTPToEmailAddress(response.getEmailAddress(), emailAddress);
    }

    private Optional<OTPEntity> checkOtpEntity(String existingEmailAddress) {
        return otpRepository.findByEmailAddress(existingEmailAddress);
    }

    private SuccessResponse sendOTPToEmailAddress(String existingEmailAddress, String emailAddress) {
        try {
            String otp = generateRandomOtp();
            saveEmailAddressOTPToDB(emailAddress, existingEmailAddress, otp);
            sendOtpEmail(emailAddress, otp);
            return new SuccessResponse(Constants.OTP_SUCCESS, HttpStatus.OK.value());
        } catch (MessagingException e) {
            log.error("Error sending OTP to email: {}", emailAddress, e);
            throw new FailedToSendOtpException(Constants.OTP_FAILED);
        }
    }

    private void saveEmailAddressOTPToDB(String emailAddress, String existingEmailAddress, String otp) {
        Optional<OTPEntity> optionalOTP = checkOtpEntity(existingEmailAddress);
        if (optionalOTP.isPresent()) {
            OTPEntity otpEntity = optionalOTP.get();
            otpEntity.setEmailAddress(emailAddress);
            otpEntity.setEmailAddressVerified(false);
            otpEntity.setEmailAddressOtpCode(otp);
            otpRepository.save(otpEntity);
            log.info("OTP saved to DB for email: {}", emailAddress);
        }
    }

    private void sendOtpEmail(String emailAddress, String otp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String htmlMsg = generateOtpEmailHtml(otp);

        helper.setText(htmlMsg, true);
        helper.setTo(emailAddress);
        helper.setSubject("cordestitch:Your OTP Code for Email Verification");
        helper.setFrom(fromEmailAddress);
        javaMailSender.send(mimeMessage);

        log.info("OTP email sent to: {}", emailAddress);
    }

    private String generateOtpEmailHtml(String otp) {
        return "<html><body>" + "<h1>OTP Verification</h1>" +
                "<p>Dear Customer,</p>" + "<p>To verify your email address, use the following OTP:</p>" +
                "<p style='font-size: 20px; font-weight: bold; color: #4CAF50;'>" + otp + "</p>" +
                "<p>This OTP is valid for 10 minutes.</p>" + "<p>If you did not request this, please ignore this email.</p>" +
                "<br/><p>Best Regards,</p>" + "<p>cordestitch Team</p></body></html>";
    }

    private String generateRandomOtp() {
        int randomNumber = random.nextInt(999999 - 100000 + 1) + 100000;
        return String.valueOf(randomNumber);
    }


    private SuccessResponse sendToPhoneNumber(UpdateRequest request) {
        log.info("Sending OTP to Phone Number: {}", request.getPhoneNumber());
        UpdateDataResponse response = checkEntity(request);
        if (isNull(response)) {
            log.error(Constants.USER_NOT_FOUND);
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }

        checkUpdatingData(response, request);

        return handleOtpForPhoneNumber(response, request.getPhoneNumber());
    }

    private SuccessResponse handleOtpForPhoneNumber(UpdateDataResponse response, String phoneNumber) {
        log.info("Handle OTP for Phone Number: {}", phoneNumber);

        Optional<OTPEntity> otpEntityOptional = checkOtpEntity(response.getEmailAddress());
        if (otpEntityOptional.isEmpty()) {
            log.error(Constants.OTP_ENTITY_DATA_NOT_FOUND + " :{}", phoneNumber);
            throw new OtpNotFoundException(Constants.OTP_ENTITY_DATA_NOT_FOUND);
        }

        return sendOTPToPhoneNumber(response.getEmailAddress(), phoneNumber);
    }

    private SuccessResponse sendOTPToPhoneNumber(String existingEmailAddress, String phoneNumber) {
        try {
            String otp = generateRandomOtp();
            log.info("Generated OTP : {}", otp);

            String apiUrl = otpServiceImplementation.buildApiUrl(phoneNumber, otp);
            log.info("API url: {}", apiUrl);

            boolean success = otpServiceImplementation.sendOtp(apiUrl);
            if (success) {
                savePhoneNumberOTPToDB(existingEmailAddress, phoneNumber, otp);
                return new SuccessResponse(Constants.OTP_SUCCESS, HttpStatus.OK.value());
            } else {
                log.error("Error while sending OTP for the Phone Number : {}", phoneNumber);
                return new SuccessResponse(Constants.OTP_FAILED, HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (IOException | URISyntaxException e) {
            log.error(Constants.OTP_FAILED + " Phone Number: {}, Message: {}", phoneNumber, e.getMessage());
            throw new FailedToSendOtpException(Constants.OTP_FAILED);
        }
    }

    private void savePhoneNumberOTPToDB(String existingEmailAddress, String phoneNumber, String otp) {
        Optional<OTPEntity> optionalOTP = checkOtpEntity(existingEmailAddress);
        if (optionalOTP.isPresent()) {
            OTPEntity otpEntity = optionalOTP.get();
            otpEntity.setPhoneNumber(phoneNumber);
            otpEntity.setOtpVerified(false);
            otpEntity.setOtpCode(otp);
            otpRepository.save(otpEntity);
            log.info("OTP saved to DB for Phone Number: {}", phoneNumber);
        }
    }

    private UserEntity findUserEntity(String userId) {
        return userRepository.findUserByUserId(userId);
    }

    private UpdateDataResponse checkInAdminEntity(String userId) {
        AdminEntity adminEntity = getAdminEntity(userId);
        log.info("Admin Entity Data : {}", adminEntity);
        return batchTheUpdateDataResponse(adminEntity.getEmailAddress(), adminEntity.isEmailAddressVerified(), adminEntity.getPhoneNumber(), adminEntity.isPhoneNumberVerified());
    }

    private UpdateDataResponse checkInAffiliateEntity(String userId) {
        AffiliateUserEntity affiliateUser = getAffiliateUserEntity(userId);
        log.info("Affiliate User Entity Data : {}", affiliateUser);
        return batchTheUpdateDataResponse(affiliateUser.getEmailAddress(), affiliateUser.isEmailAddressVerified(), affiliateUser.getPhoneNumber(), affiliateUser.isPhoneNumberVerified());
    }

    private UpdateDataResponse checkInUserEntity(String userId) {
        UserEntity userEntity = findUserEntity(userId);
        log.info("User Entity Data : {}", userEntity);

        if (isNull(userEntity)) {
            log.info(Constants.USER_NOT_FOUND);
            throw new UserNotFoundException(Constants.USER_NOT_FOUND);
        }
        return batchTheUpdateDataResponse(userEntity.getEmailAddress(), userEntity.isEmailAddressVerified(), userEntity.getPhoneNumber(), userEntity.isPhoneNumberVerified());
    }

    private SuccessResponse updateProfileForAffiliateUserEntity(UpdateProfileRequest request) {
        AffiliateUserEntity affiliateUser = getAffiliateUserEntity(request.getUserId());
        log.info("Affiliate User Entity Data : {}", affiliateUser);

        UpdateDataResponse response = checkSameDataOrNot(affiliateUser, request);
        log.info(Constants.UPDATE_DATA_RESPONSE, response);

        if (affiliateUser.isEmailAddressVerified() || affiliateUser.isPhoneNumberVerified()) {
            affiliateUser.setFirstName(request.getFirstName());
            affiliateUser.setLastName(request.getLastName());
            affiliateUser.setGender(request.getGender());
            affiliateUser.setEmailAddress(response.getEmailAddress());
            affiliateUser.setPhoneNumber(response.getPhoneNumber());
            affiliateUser.setEmailAddressVerified(response.isEmailAddressVerified());
            affiliateUser.setPhoneNumberVerified(response.isPhoneNumberVerified());
            affiliateUserRepository.save(affiliateUser);
            log.info("Affiliate User Entity Updated : {}", affiliateUser);
        }
        return new SuccessResponse(Constants.PROFILE_UPDATE_MSG, HttpStatus.OK.value());
    }

    private SuccessResponse updateProfileForAdminEntity(UpdateProfileRequest request) {
        AdminEntity adminEntity = getAdminEntity(request.getUserId());
        log.info("Admin Entity Data : {}", adminEntity);

        UpdateDataResponse response = checkSameDataOrNot(adminEntity, request);
        log.info(Constants.UPDATE_DATA_RESPONSE, response);

        if (adminEntity.isEmailAddressVerified() || adminEntity.isPhoneNumberVerified()) {
            adminEntity.setFirstName(request.getFirstName());
            adminEntity.setLastName(request.getLastName());
            adminEntity.setGender(request.getGender());
            adminEntity.setEmailAddress(response.getEmailAddress());
            adminEntity.setPhoneNumber(response.getPhoneNumber());
            adminEntity.setEmailAddressVerified(response.isEmailAddressVerified());
            adminEntity.setPhoneNumberVerified(response.isPhoneNumberVerified());
            adminRepository.save(adminEntity);
            log.info("Admin Entity Updated : {}", adminEntity);
        }
        return new SuccessResponse(Constants.PROFILE_UPDATE_MSG, HttpStatus.OK.value());
    }

    private SuccessResponse updateProfileForUserEntity(UpdateProfileRequest request) {
        UserEntity userEntity = findUserEntity(request.getUserId());
        log.info("User Entity Data : {}", userEntity);

        UpdateDataResponse response = checkSameDataOrNot(userEntity, request);
        log.info(Constants.UPDATE_DATA_RESPONSE, response);

        if (userEntity.isEmailAddressVerified() || userEntity.isPhoneNumberVerified()) {
            userEntity.setFirstName(request.getFirstName());
            userEntity.setLastName(request.getLastName());
            userEntity.setGender(request.getGender());
            userEntity.setEmailAddress(response.getEmailAddress());
            userEntity.setPhoneNumber(response.getPhoneNumber());
            userEntity.setEmailAddressVerified(response.isEmailAddressVerified());
            userEntity.setPhoneNumberVerified(response.isPhoneNumberVerified());
            userRepository.save(userEntity);
            log.info("User Entity Updated : {}", userEntity);
        }
        return new SuccessResponse(Constants.PROFILE_UPDATE_MSG, HttpStatus.OK.value());
    }

    private UpdateDataResponse checkSameDataOrNot(Object entity, UpdateProfileRequest request) {
        UpdateDataResponse response = new UpdateDataResponse();

        try {
            Method getEmailMethod = entity.getClass().getMethod("getEmailAddress");
            Method getPhoneMethod = entity.getClass().getMethod("getPhoneNumber");
            Method isEmailVerifiedMethod = entity.getClass().getMethod("isEmailAddressVerified");
            Method isPhoneVerifiedMethod = entity.getClass().getMethod("isPhoneNumberVerified");

            String emailAddress = (String) getEmailMethod.invoke(entity);
            String phoneNumber = (String) getPhoneMethod.invoke(entity);
            boolean isEmailVerified = (boolean) isEmailVerifiedMethod.invoke(entity);
            boolean isPhoneVerified = (boolean) isPhoneVerifiedMethod.invoke(entity);

            response.setEmailAddress(emailAddress);
            response.setPhoneNumber(phoneNumber);

            if (isEmailVerified) {
                boolean isSameEmail = emailAddress.equals(request.getEmailAddress());
                if (!isSameEmail) {
                    throw new UserDetailsMissMatchException(Constants.EMAIL_MISMATCH);
                }
                response.setEmailAddressVerified(true);
            }

            if (isPhoneVerified) {
                boolean isSamePhoneNumber = phoneNumber.equals(request.getPhoneNumber());
                if (!isSamePhoneNumber) {
                    throw new UserDetailsMissMatchException(Constants.PHONE_NUMBER_MISMATCH);
                }
                response.setPhoneNumberVerified(true);
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.info(Constants.SAME_DATA_ERROR_MSG, e.getMessage());
            throw new DataCheckReflectionException(Constants.SAME_DATA_ERROR_MSG, e);
        }

        return response;
    }
}