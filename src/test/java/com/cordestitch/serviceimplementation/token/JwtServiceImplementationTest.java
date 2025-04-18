package com.cordestitch.serviceimplementation.token;

import com.cordestitch.exception.token.InvalidRefreshTokenException;
import com.cordestitch.exception.token.JwtProcessingException;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.response.token.AuthTokenResponse;
import com.cordestitch.response.token.SessionStatusResponse;
import com.cordestitch.service.serviceimplementation.token.CookieService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import com.cordestitch.util.Constants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplementationTest {
    @InjectMocks
    private JwtServiceImplementation jwtServiceImplementation;

    @Mock
    private IdEncryptor idEncryptor;

    @Mock
    private CookieService cookieService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Claims claims;

    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";


    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        MockitoAnnotations.openMocks(this);

        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        String secureKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        ReflectionTestUtils.setField(jwtServiceImplementation, "secretKey", secureKey);
        ReflectionTestUtils.setField(jwtServiceImplementation, "jwtExpiration", 3600000);
        ReflectionTestUtils.setField(jwtServiceImplementation, "environment", "local");

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);
    }


    @Test
    void generateToken() {
        String validToken = jwtServiceImplementation.generateToken(DECRYPTED_USER_ID, anyString());
        assertNotNull(validToken);
    }

    @Test
    void isTokenValid_Success(){
        String validToken = jwtServiceImplementation.generateToken(DECRYPTED_USER_ID, anyString());
        assertTrue(jwtServiceImplementation.isTokenValid(validToken));
    }

    @Test
    void isTokenValid_Exception(){
        String invalidToken = "Bearer " + jwtServiceImplementation.generateToken(DECRYPTED_USER_ID, anyString());
        JwtProcessingException exception = assertThrows(JwtProcessingException.class, ()->jwtServiceImplementation.isTokenValid(invalidToken));
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void extractUserId(){
        String token = jwtServiceImplementation.generateToken(ENCRYPTED_USER_ID, anyString());
        assertEquals(ENCRYPTED_USER_ID, jwtServiceImplementation.extractUserId(token));
    }

    @Test
    void isTokenExpired(){
        String token = jwtServiceImplementation.generateToken(ENCRYPTED_USER_ID, anyString());
        assertFalse(jwtServiceImplementation.isTokenExpired(token));
    }

    @Test
    void generateRefreshToken() {
        String validToken = jwtServiceImplementation.generateRefreshToken(ENCRYPTED_USER_ID, anyString());
        assertNotNull(validToken);
    }

    @Test
    void refreshToken_Successful() {
        ReflectionTestUtils.setField(jwtServiceImplementation, "refreshTokenExpiration", 86400000);

        String validRefreshToken = jwtServiceImplementation.generateRefreshToken(DECRYPTED_USER_ID, anyString());
        Cookie refreshTokenCookie = new Cookie("RefreshToken", validRefreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        jwtServiceImplementation.isTokenValid(validRefreshToken);
        jwtServiceImplementation.isTokenExpired(validRefreshToken);
        jwtServiceImplementation.extractUserId(validRefreshToken);

        AuthTokenResponse authTokenResponse = jwtServiceImplementation.refreshToken(request, this.response);

        assertNotNull(authTokenResponse.getAccessToken());
        verify(cookieService).clearExistingCookie(this.response);
    }

    @Test
    void refreshToken_Successful_With_Different_CookieName() {
        ReflectionTestUtils.setField(jwtServiceImplementation, "refreshTokenExpiration", 86400000);

        String validRefreshToken = jwtServiceImplementation.generateRefreshToken(DECRYPTED_USER_ID, anyString());
        Cookie refreshTokenCookie = new Cookie("token", validRefreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        jwtServiceImplementation.isTokenValid(validRefreshToken);
        jwtServiceImplementation.isTokenExpired(validRefreshToken);
        jwtServiceImplementation.extractUserId(validRefreshToken);

        InvalidRefreshTokenException exception = assertThrows(InvalidRefreshTokenException.class, ()->jwtServiceImplementation.refreshToken(request, this.response));
        assertEquals("Refresh token not found in cookies",exception.getMessage());
    }

    @Test
    void refreshToken_Exception_NoRefreshTokenInCookies() {
        when(request.getCookies()).thenReturn(null);

        assertThrows(InvalidRefreshTokenException.class, () -> jwtServiceImplementation.refreshToken(request, response));
    }

    @Test
    void validateSession_Success(){
        ReflectionTestUtils.setField(jwtServiceImplementation, "refreshTokenExpiration", 86400000);

        String validRefreshToken = jwtServiceImplementation.generateToken(DECRYPTED_USER_ID, anyString());
        Cookie refreshTokenCookie = new Cookie("RefreshToken", validRefreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validRefreshToken);
        SessionStatusResponse statusResponse = jwtServiceImplementation.validateSession(request, response);
        assertEquals(Constants.SESSION_ACTIVE,statusResponse.getMessage());

    }

    @Test
    void validateSession_Success_When_Session_Is_About_To_Expire(){
        ReflectionTestUtils.setField(jwtServiceImplementation, "refreshTokenExpiration", 120000);
        String validRefreshToken = jwtServiceImplementation.generateRefreshToken(DECRYPTED_USER_ID, anyString());
        Instant nearExpiryTime = Instant.now().plusSeconds(100); // Token expires within the 120 seconds buffer
        when(claims.getExpiration()).thenReturn(Date.from(nearExpiryTime));
        Cookie refreshTokenCookie = new Cookie("RefreshToken", validRefreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{refreshTokenCookie});
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validRefreshToken);
        SessionStatusResponse statusResponse = jwtServiceImplementation.validateSession(request, response);
        assertEquals(Constants.SESSION_ABOUT_TO_EXPIRE,statusResponse.getMessage());

    }

    @Test
    void extractEncryptedUserId(){
        String encryptedUserId = jwtServiceImplementation.generateToken(ENCRYPTED_USER_ID, anyString());
        assertNull(jwtServiceImplementation.extractEncryptedUserId(encryptedUserId));
    }
}