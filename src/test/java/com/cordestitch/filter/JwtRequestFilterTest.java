package com.cordestitch.filter;

import com.cordestitch.service.service.token.JwtService;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtRequestFilterTest {
    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Mock
    private JwtServiceImplementation jwtServiceImplementation;

    @Mock
    private JwtService jwtService;

    @Mock
    private Claims claims;

    @Mock
    private IdEncryptor idEncryptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;
    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        ReflectionTestUtils.setField(jwtServiceImplementation, "secretKey", "secretKeys");
        ReflectionTestUtils.setField(jwtServiceImplementation, "jwtExpiration", 3600000);

        when(idEncryptor.encrypt(DECRYPTED_USER_ID))
                .thenReturn(ENCRYPTED_USER_ID);
    }

    @Test
    void testDoFilterInternalWhitelistedPath() throws ServletException, IOException {
        request.setRequestURI("/api/user/login");
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_Exception_When_Request_Is_Null(){
        assertThrows(NullPointerException.class, ()->jwtRequestFilter.doFilterInternal(null,response,filterChain));
    }

    @Test
    void testDoFilterInternal_Exception_When_Response_Is_Null(){
        assertThrows(NullPointerException.class, ()->jwtRequestFilter.doFilterInternal(request,null,filterChain));
    }

    @Test
    void testDoFilterInternal_Exception_When_FilterChain_Is_Null(){
        assertThrows(NullPointerException.class, ()->jwtRequestFilter.doFilterInternal(request,response,null));
    }

    @Disabled
    @Test
    void testDoFilterInternalWithValidToken() throws Exception {
        String token = "mock.valid.token";
        when(jwtService.generateToken(DECRYPTED_USER_ID, anyString())).thenReturn(token);

        String securedPath = "/api/user/securedPath";
        request.setRequestURI(securedPath);
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEncryptedUserId(token)).thenReturn("encryptedUserId");
        when(idEncryptor.decrypt("encryptedUserId")).thenReturn(DECRYPTED_USER_ID);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).isTokenValid(token);
        verify(jwtService, times(1)).extractEncryptedUserId(token);
        verify(idEncryptor, times(1)).decrypt("encryptedUserId");
        verify(filterChain, times(1)).doFilter(request, response);

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(DECRYPTED_USER_ID, authentication.getPrincipal());
        assertEquals(1, authentication.getAuthorities().size());
        assertEquals("ROLE_USER", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testDoFilterInternalWithValidToken_False() throws ServletException, IOException {
        request.setRequestURI("/api/user/home");
        String validToken = jwtService.generateToken(DECRYPTED_USER_ID, "ROLE_USER");
        request.addHeader("auth", "start " + validToken);
        jwtService.isTokenValid(request.getHeader(any()));
        when(jwtService.extractUserId(any())).thenReturn("user123");
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(0)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testDoFilterInternal_When_AuthHeader_Not_Starting_With_Bearer() throws ServletException, IOException {
        request.setRequestURI("/api/secure/resource");
        String invalidToken = "invalidJwtToken";
        request.addHeader("Authorization", "start " + invalidToken);
        jwtService.isTokenValid(invalidToken);
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(0)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Disabled
    @Test
    void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        request.setRequestURI("/api/secure/resource");
        String invalidToken = "invalidJwtToken";
        request.addHeader("Authorization", "Bearer " + invalidToken);
        when(jwtService.isTokenValid(invalidToken)).thenReturn(false);
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(0)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testDoFilterInternalWithoutAuthorizationHeader() throws ServletException, IOException {
        request.setRequestURI("/api/secure/resource");
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(0)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void shouldNotFilter_When_Path_Is_WhiteListed(){
        request.setRequestURI("/api/user/login");
        assertTrue(jwtRequestFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_When_Path_Is_Not_WhiteListed(){
        request.setRequestURI("/api/user/home");
        assertFalse(jwtRequestFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_When_Request_Is_Null() {
        assertThrows(NullPointerException.class, () -> jwtRequestFilter.shouldNotFilter(null),
                "Expected shouldNotFilter to throw NullPointerException when request is null.");
    }

}