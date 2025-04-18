package com.cordestitch.controller.token;

import com.cordestitch.response.token.AuthTokenResponse;
import com.cordestitch.response.token.SessionStatusResponse;
import com.cordestitch.service.service.token.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jwt")
@RequiredArgsConstructor
public class JwtController {

    private final JwtService jwtService;

    @GetMapping("/getAccessToken")
    public ResponseEntity<AuthTokenResponse> getAccessToken(@Valid @RequestParam String userId , @NotNull String userType) {
        String accessToken = jwtService.generateToken(userId, userType);
        String refreshToken = jwtService.generateRefreshToken(userId, userType);
        String tokenExpiryTime = jwtService.extractTokenExpiryTime(accessToken);
        String refreshTokenExpiryTime = jwtService.extractTokenExpiryTime(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(new AuthTokenResponse(accessToken, tokenExpiryTime, refreshTokenExpiryTime, HttpStatus.OK.value()));
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<AuthTokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        AuthTokenResponse accessToken = jwtService.refreshToken(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(accessToken);
    }

    @GetMapping("/sessionStatus")
    public ResponseEntity<SessionStatusResponse> sessionStatus(HttpServletRequest request, HttpServletResponse response) {
        SessionStatusResponse sessionStatus = jwtService.validateSession(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(sessionStatus);
    }
}
