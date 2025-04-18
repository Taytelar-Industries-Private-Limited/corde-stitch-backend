package com.cordestitch.controller.otp;

import com.cordestitch.request.otp.OTPRequest;
import com.cordestitch.request.otp.ValidateOTP;
import com.cordestitch.response.otp.OTPResponse;
import com.cordestitch.service.service.otp.OTPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OTPController {

    private final OTPService otpService;

    /**
     * Endpoint for OTP verification.
     *
     * @param validateOTP The request object containing phone number and the OTP entered by the user.
     *                    It is validated using @Valid to ensure the phone number and OTP are provided correctly.
     * @return A ResponseEntity containing the OTPResponse object and HTTP status 200 (OK) if OTP verification is successful.
     *         If the OTP verification fails, an appropriate error response will be returned.
     */
    @PostMapping("/verifyOtp")
    public ResponseEntity<OTPResponse> verifyOtp(@Valid @RequestBody ValidateOTP validateOTP){
        OTPResponse response = otpService.verifyOtp(validateOTP);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    /**
     * Endpoint to generate an OTP and send it to the user's phone number.
     *
     * @param otpRequest The phone number for which the OTP should be generated. It must not be blank.
     * @return A ResponseEntity containing the OTPResponse object and HTTP status 200 (OK) if OTP generation is successful.
     *         This response object typically contains the message indicating that the OTP has been sent.
     */
    @PostMapping("/sendOtp")
    public  ResponseEntity<OTPResponse> generateOtp(@Valid @RequestBody OTPRequest otpRequest) {
        OTPResponse response = otpService.generateOtp(otpRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/sendWhatsAppOtp")
    public  ResponseEntity<OTPResponse> generateWhatsAppOtp(@Valid @RequestBody OTPRequest otpRequest) {
        OTPResponse response = otpService.generateWhatsAppOtp(otpRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
