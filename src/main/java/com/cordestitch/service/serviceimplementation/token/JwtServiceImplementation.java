package com.cordestitch.service.serviceimplementation.token;

import com.cordestitch.exception.token.InvalidRefreshTokenException;
import com.cordestitch.exception.token.JwtProcessingException;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.response.token.AuthTokenResponse;
import com.cordestitch.response.token.SessionStatusResponse;
import com.cordestitch.service.service.token.JwtService;
import com.cordestitch.util.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImplementation implements JwtService {

    private final CookieService cookieService;

    private final IdEncryptor idEncryptor;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String USER_ID = "userId";
    private static final String ROLES = "roles";
    private static final String ENV = "environment";

    @Value("${spring.app.env}")
    private String environment;

    @Override
    public String generateToken(String userId, String userType) {
        String encryptedUserId = idEncryptor.encrypt(userId);
        log.info("Encrypted userId to generate access token : {}", encryptedUserId);
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID, encryptedUserId);
        claims.put(ROLES, userType);
        claims.put(ENV, environment);

        return buildToken(claims, userId, jwtExpiration);
    }

    @Override
    public String generateRefreshToken(String userId, String userType) {
        String encryptedUserId = idEncryptor.encrypt(userId);
        log.info("Encrypted userId to generate refresh token : {}", encryptedUserId);
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID, encryptedUserId);
        claims.put(ROLES, userType);
        claims.put(ENV, environment);

        return buildToken(claims, userId, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, String userId, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public AuthTokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        log.info("Extracted Refresh Token : {}", refreshToken);

        if (!isTokenValid(refreshToken) || isTokenExpired(refreshToken)) {
            log.error(Constants.INVALID_REFRESH_TOKEN + " : {}", refreshToken);
            throw new InvalidRefreshTokenException(Constants.INVALID_REFRESH_TOKEN);
        }

        String userId = extractUserId(refreshToken);
        log.info("Extracted UserID : {}", userId);

        String userType = extractUserType(refreshToken);
        log.info("Extracted User Type : {}", userType);

        String newAccessToken = generateToken(userId, userType);
        log.info("New Access Token : {}", newAccessToken);
        String newRefreshToken = generateRefreshToken(userId, userType);
        log.info("New Refresh Token : {}", newRefreshToken);

        String tokenExpiryTime = extractTokenExpiryTime(newAccessToken);
        String refreshTokenExpiryTime = extractTokenExpiryTime(newRefreshToken);
        log.info("Token Expiry Time : {}", tokenExpiryTime);
        log.info("Refresh Token Expiry Time : {}", refreshTokenExpiryTime);

        cookieService.clearExistingCookie(response);
        cookieService.createJwtCookie(newAccessToken, response);
        cookieService.createRefreshCookie(newRefreshToken, response);
        return new AuthTokenResponse(newAccessToken, tokenExpiryTime, refreshTokenExpiryTime, HttpStatus.OK.value());
    }

    @Override
    public String extractTokenExpiryTime(String newAccessToken) {
        Claims claims = parseClaimsJwsToken(newAccessToken);
        Date expirationDate = claims.getExpiration();
        Instant expiryInstant = expirationDate.toInstant();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneId.of("Asia/Kolkata"));

        return formatter.format(expiryInstant);
    }


    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaimsJwsToken(token);
            String tokenEnv = claims.get(ENV, String.class);

            if (!environment.equals(tokenEnv)) {
                log.error("Token environment mismatch: expected {}, found {}", environment, tokenEnv);
                return false;
            }
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public String extractEncryptedUserId(String token) {
        Claims claims = parseClaimsJwsToken(token);
        return claims.get(USER_ID, String.class);
    }

    @Override
    public String extractUserId(String token) {
        Claims claims = parseClaimsJwsToken(token);
        return claims.get("sub", String.class);
    }

    @Override
    public String extractUserType(String refreshToken) {
        Claims claims = parseClaimsJwsToken(refreshToken);
        return claims.get(ROLES, String.class);
    }

    @Override
    public boolean isTokenExpired(String token) {
        Claims claims = parseClaimsJwsToken(token);
        return claims.getExpiration().before(new Date());
    }

    @Override
    public SessionStatusResponse validateSession(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        log.info("Extracted Refresh Token : {}", refreshToken);

        Instant refreshTokenExpiryTime = getRefreshTokenExpiryTime(refreshToken);
        log.info("Refresh Token Expiry Time : {}", refreshTokenExpiryTime);
        Instant currentTime = Instant.now();
        log.info("Current Time : {}", currentTime);

        boolean isAboutToExpire = refreshTokenExpiryTime.minusSeconds(120).isBefore(currentTime);
        log.info("Is About To Expire : {}", isAboutToExpire);

        SessionStatusResponse statusResponse;
        if (isAboutToExpire) {
            statusResponse = new SessionStatusResponse(Constants.SESSION_ABOUT_TO_EXPIRE, true);
        } else {
            statusResponse = new SessionStatusResponse(Constants.SESSION_ACTIVE, false);
        }

        log.info("Session Status Response : {}", statusResponse);
        return statusResponse;
    }

    private Instant getRefreshTokenExpiryTime(String refreshToken) {
        try {
            Claims claims = parseClaimsJwsToken(refreshToken);

            Date expirationDate = claims.getExpiration();
            return expirationDate.toInstant();

        } catch (JwtException e) {
            log.info(Constants.EXTRACT_EXPIRATION_TIME_ERROR_MESSAGE + " JwtException: " + e.getMessage());
            throw new JwtProcessingException(Constants.EXTRACT_EXPIRATION_TIME_ERROR_MESSAGE);
        }
    }

    private Claims parseClaimsJwsToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw new JwtProcessingException("Invalid JWT token");
        }
    }

    private String extractRefreshToken(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("RefreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            log.error("Refresh token not found in cookies");
            throw new InvalidRefreshTokenException("Refresh token not found in cookies");
        }
        return refreshToken;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

