package com.cordestitch.service.serviceimplementation.otp;

import com.cordestitch.entity.otp.OTPEntity;
import com.cordestitch.exception.otp.InvalidOTPException;
import com.cordestitch.exception.otp.OtpNotFoundException;
import com.cordestitch.exception.otp.UnknownUserTypeException;
import com.cordestitch.exception.user.UserAccountAlreadyExistException;
import com.cordestitch.exception.user.UserAccountNotExistException;
import com.cordestitch.repository.otp.OTPRepository;
import com.cordestitch.request.otp.OTPRequest;
import com.cordestitch.request.otp.ValidateOTP;
import com.cordestitch.response.SuccessResponse;
import com.cordestitch.response.otp.OTPResponse;
import com.cordestitch.service.service.otp.OTPService;
import com.cordestitch.service.service.whatsapp.WhatsAppService;
import com.cordestitch.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class OTPServiceImplementation implements OTPService {

    private final OTPRepository otpRepository;

    private final WhatsAppService whatsAppService;

    private final Random random = new Random();

    private static final String VARIABLE_VALUES = "&variables_values=";

    private static final String ROUTE_NUMBERS = "&route=otp&numbers=";

    @Value("${fast2sms.api.key}")
    private String apiKey;

    @Value("${fast2sms.sms.url}")
    private String smsUrl;

    @Override
    public OTPResponse generateOtp(OTPRequest otpRequest) {
        log.info("Generate OTP Request : {}", otpRequest);
        try {
            String otp = generateRandomOTP();
            log.info("Generated OTP : {}", otp);

            log.info("API Key : {}, SMS url:{}", apiKey, smsUrl);

            String apiUrl = buildApiUrl(otpRequest.getPhoneNumber(), otp);
            log.info("API url: {}", apiUrl);
            boolean success = sendOtp(apiUrl);

            return buildOtpResponse(success, otpRequest, otp);

        } catch (UserAccountNotExistException | UserAccountAlreadyExistException | UnknownUserTypeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while generating OTP: {}", e.getMessage());
            return buildErrorResponse();
        }
    }

    @Override
    public OTPResponse generateWhatsAppOtp(OTPRequest otpRequest) {
        String otp = generateRandomOTP();
        log.info("Generated OTP : {}", otp);

        SuccessResponse successResponse = whatsAppService.generateOtp(otpRequest.getPhoneNumber(),otp);
        boolean success = successResponse.getStatusCode() == HttpStatus.OK.value();
        return buildOtpResponse(success, otpRequest, otp);
    }

    @Override
    public OTPResponse verifyOtp(ValidateOTP validateOTP) {
        log.info("Validating OTP Request: {}", validateOTP);
        OTPEntity otpEntity = otpRepository.findByPhoneNumber(validateOTP.getPhoneNumber());

        if(isNull(otpEntity)) {
            log.error(Constants.OTP_ENTITY_NOT_FOUND);
            throw new OtpNotFoundException(Constants.OTP_ENTITY_NOT_FOUND);
        }

        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of(Constants.ZONE));
        LocalDateTime otpCreatedTime = otpEntity.getOtpCreatedAt();
        Duration duration = Duration.between(otpCreatedTime, currentTime);

        if (duration.toSeconds() > 60) {
            log.warn(Constants.OTP_EXPIRED);
            throw new InvalidOTPException(Constants.OTP_EXPIRED);
        }

        List<String> types = Arrays.stream(otpEntity.getUserType().split(",")).toList();
        boolean userType = types.contains(validateOTP.getUserType());
        log.info("User Type : {} ", userType);

        if (otpEntity.getOtpCode().equals(validateOTP.getOtpPassword()) && userType) {
            otpEntity.setOtpVerified(true);
            otpRepository.save(otpEntity);
            return otpResponseSuccess();
        } else {
            return otpResponseFailed();
        }
    }

    public String buildApiUrl(String phoneNumber, String otp) {
        return smsUrl + apiKey +
                VARIABLE_VALUES + otp +
                ROUTE_NUMBERS + phoneNumber;
    }

    public boolean sendOtp(String apiUrl) throws IOException, URISyntaxException {
        URI url = new URI(apiUrl);
        HttpURLConnection connection = createConnection(url.toURL());
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        log.info("Response code while sending OTP: {}", responseCode);
        return responseCode == 200;
    }

    private OTPResponse buildOtpResponse(boolean success, OTPRequest otpRequest, String otp) {
        OTPResponse otpResponse = new OTPResponse();
        if (success) {
            saveOTPToDB(otpRequest, otp);
            otpResponse.setMessage(Constants.OTP_SUCCESS);
            otpResponse.setStatusCode(HttpStatus.OK.value());
        } else {
            otpResponse.setMessage(Constants.OTP_FAILED);
            otpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        log.info("OTP response: {}", otpResponse);
        return otpResponse;
    }

    private OTPResponse buildErrorResponse() {
        OTPResponse otpResponse = new OTPResponse();
        otpResponse.setMessage("Exception while sending OTP.. ");
        otpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return otpResponse;
    }

    private void saveOTPToDB(OTPRequest otpRequest, String otp) {
        OTPEntity entity = otpRepository.findByPhoneNumber(otpRequest.getPhoneNumber());
        if (entity == null) {
            OTPEntity otpEntity = new OTPEntity();
            otpEntity.setPhoneNumber(otpRequest.getPhoneNumber());
            otpEntity.setOtpCode(otp);
            otpEntity.setOtpVerified(false);
            otpEntity.setUserType(otpRequest.getUserType());
            otpEntity.setEmailAddressVerified(false);
            otpEntity.setOtpCreatedAt(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
            log.info("OTP entity: {}", otpEntity);
            otpRepository.save(otpEntity);
        } else {
            entity.setOtpCode(otp);
            entity.setOtpVerified(false);
            entity.setOtpCreatedAt(LocalDateTime.now(ZoneId.of(Constants.ZONE)));
            List<String> userTypes = Arrays.asList(entity.getUserType().split(","));
            if (!userTypes.contains(otpRequest.getUserType())) {
                entity.setUserType(entity.getUserType() + "," + otpRequest.getUserType());
            }
            log.info("Updated OTP entity: {}", entity);
            otpRepository.save(entity);
        }
    }

    public HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private String generateRandomOTP() {
        int randomNumber = random.nextInt(999999 - 100000 + 1) + 100000;
        return String.valueOf(randomNumber);
    }

    private OTPResponse otpResponseSuccess() {
        OTPResponse otpResponse = new OTPResponse();
        otpResponse.setMessage(Constants.OTP_VERIFIED_SUCCESSFULLY);
        otpResponse.setStatusCode(HttpStatus.OK.value());
        log.info("OTP Success Response {}", otpResponse);
        return otpResponse;
    }

    private OTPResponse otpResponseFailed() {
        OTPResponse otpResponse = new OTPResponse();
        otpResponse.setMessage(Constants.OTP_VERIFIED_FAILED);
        otpResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        log.info("OTP Failed Response {}", otpResponse);
        return otpResponse;
    }
}