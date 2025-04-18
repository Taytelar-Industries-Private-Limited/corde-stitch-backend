package com.cordestitch.service.service.token;

import com.cordestitch.response.token.AuthTokenResponse;
import com.cordestitch.response.token.SessionStatusResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface JwtService {
    String generateToken(String userId, String userType);

    String generateRefreshToken(String id, String userType);

    AuthTokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    boolean isTokenValid(String token);

    String extractEncryptedUserId(String token);

    String extractUserId(String token);

    boolean isTokenExpired(String token);

    SessionStatusResponse validateSession(HttpServletRequest request, HttpServletResponse response);

    String extractTokenExpiryTime(String accessToken);

    String extractUserType(String token);
}
