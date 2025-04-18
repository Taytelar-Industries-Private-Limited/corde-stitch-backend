package com.cordestitch.serviceimplementation.token;

import com.cordestitch.service.serviceimplementation.token.CookieService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class CookieServiceTest {
    @InjectMocks
    private CookieService cookieService;

    @Mock
    private JwtServiceImplementation jwtServiceImplementation;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void clearExistingCookie(){
        cookieService.clearExistingCookie(response);
        verify(response, times(2)).addCookie(argThat(cookie ->
                ("JWT".equals(cookie.getName()) && cookie.getValue() == null && cookie.getMaxAge() == 0)
                        || ("RefreshToken".equals(cookie.getName()) && cookie.getValue() == null && cookie.getMaxAge() == 0)
        ));
    }

    @Test
    void createJwtCookie(){
        String jwtToken = "token";
        cookieService.createJwtCookie(jwtToken, response);

        verify(response).addCookie(argThat(cookie ->
                "JWT".equals(cookie.getName()) &&
                        jwtToken.equals(cookie.getValue()) &&
                        cookie.getMaxAge() == 3600
        ));
    }

    @Test
    void createRefreshCookie(){
        String refreshToken = "refreshToken";
        cookieService.createRefreshCookie(refreshToken, response);

        verify(response).addCookie(argThat(cookie ->
                "RefreshToken".equals(cookie.getName()) &&
                        refreshToken.equals(cookie.getValue()) &&
                        cookie.getMaxAge() == 86400
        ));
    }
}