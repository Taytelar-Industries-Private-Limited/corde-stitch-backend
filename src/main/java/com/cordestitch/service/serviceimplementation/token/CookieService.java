package com.cordestitch.service.serviceimplementation.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CookieService {

    @Value("${spring.app.env}")
    private String environment;


    public void clearExistingCookie(HttpServletResponse response) {
        Cookie existingCookie = new Cookie("JWT", null);
        existingCookie.setHttpOnly(true);
        existingCookie.setSecure(false);
        existingCookie.setPath("/");
        existingCookie.setMaxAge(0);
        response.addCookie(existingCookie);

        Cookie refreshCookie = new Cookie("RefreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    public boolean isProduction(String environment) {
        return "prod".equalsIgnoreCase(environment) || "PROD".equalsIgnoreCase(environment);
    }

    public void createJwtCookie(String jwtToken, HttpServletResponse response) {

        Cookie cookie = new Cookie("JWT", jwtToken);
        cookie.setHttpOnly(false);
        cookie.setSecure(isProduction(environment));
        cookie.setPath("/");
        cookie.setMaxAge(3600); //-> 1 hour
        log.info("Cookie Object Created : {}", cookie);

        response.addCookie(cookie);
    }

    public void createRefreshCookie(String refreshToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("RefreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(isProduction(environment));
        cookie.setPath("/");
        cookie.setMaxAge(86400); //-> 1 day
        log.info("Cookie Object Created : {}", cookie);

        response.addCookie(cookie);
    }

    public void createCookie(String token, String sessionId, String deviceId, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("tokenId", token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(isProduction(environment));
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(2 * 24 * 60 * 60);
        log.info("Cookie Object Created For TokenId : {}", tokenCookie);

        Cookie sessionIdCookie = new Cookie("id", sessionId);
        sessionIdCookie.setHttpOnly(true);
        sessionIdCookie.setSecure(isProduction(environment));
        sessionIdCookie.setPath("/");
        sessionIdCookie.setMaxAge(2 * 24 * 60 * 60);
        log.info("Cookie Object Created For SessionId : {}", sessionIdCookie);

        Cookie deviceIdCookie = new Cookie("ip", deviceId);
        deviceIdCookie.setHttpOnly(true);
        deviceIdCookie.setSecure(isProduction(environment));
        deviceIdCookie.setPath("/");
        deviceIdCookie.setMaxAge(2 * 24 * 60 * 60);
        log.info("Cookie Object Created For DeviceId : {}", deviceIdCookie);

        response.addCookie(tokenCookie);
        response.addCookie(sessionIdCookie);
        response.addCookie(deviceIdCookie);
    }

    public void clearCookies(HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("tokenId", null);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(isProduction(environment));
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        log.info("Cookie Object Cleared For TokenId : {}", tokenCookie);

        Cookie sessionIdCookie = new Cookie("Id", null);
        sessionIdCookie.setHttpOnly(true);
        sessionIdCookie.setSecure(isProduction(environment));
        sessionIdCookie.setPath("/");
        sessionIdCookie.setMaxAge(0);
        log.info("Cookie Object Cleared For SessionId : {}", sessionIdCookie);

        Cookie deviceIdCookie = new Cookie("ip", null);
        deviceIdCookie.setHttpOnly(true);
        deviceIdCookie.setSecure(isProduction(environment));
        deviceIdCookie.setPath("/");
        deviceIdCookie.setMaxAge(0);
        log.info("Cookie Object Cleared For DeviceId : {}", deviceIdCookie);

        response.addCookie(tokenCookie);
        response.addCookie(sessionIdCookie);
        response.addCookie(deviceIdCookie);
    }
}
