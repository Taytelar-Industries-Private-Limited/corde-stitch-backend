package com.cordestitch.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestParameterDecryptionFilterTest {
    @Mock
    private IdEncryptor idEncryptor;

    private static final String SECRET_KEY = "1234567890123456"; // 16 characters for AES-128 key


    @InjectMocks
    private RequestParameterDecryptionFilter requestParameterDecryptionFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        ReflectionTestUtils.setField(idEncryptor, "secretKey", SECRET_KEY);

    }

    @Test
    void doFilterInternal_ShouldDecryptUserId_WhenUserIdParameterIsPresent() throws ServletException, IOException {
        String encryptedUserId = "encryptedUserId";
        String decryptedUserId = "12345";
        request.setMethod("GET");
        request.setParameter("userId", encryptedUserId);
        when(idEncryptor.decrypt(encryptedUserId)).thenReturn(decryptedUserId);
        requestParameterDecryptionFilter.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_Exception_When_Request_Is_Null() throws ServletException, IOException {
        String encryptedUserId = "encryptedUserId";
        String decryptedUserId = "12345";
        request.setMethod("GET");
        request.setParameter("userId", encryptedUserId);
        when(idEncryptor.decrypt(encryptedUserId)).thenReturn(decryptedUserId);
        assertThrows(NullPointerException.class, ()->requestParameterDecryptionFilter.doFilterInternal(null, response, filterChain));
    }

    @Test
    void doFilterInternal_Exception_When_Response_Is_Null() throws ServletException, IOException {
        String encryptedUserId = "encryptedUserId";
        String decryptedUserId = "12345";
        request.setMethod("GET");
        request.setParameter("userId", encryptedUserId);
        when(idEncryptor.decrypt(encryptedUserId)).thenReturn(decryptedUserId);
        assertThrows(NullPointerException.class, ()->requestParameterDecryptionFilter.doFilterInternal(request, null, filterChain));
    }

    @Test
    void doFilterInternal_Exception_When_FilterChain_Is_Null() throws ServletException, IOException {
        String encryptedUserId = "encryptedUserId";
        String decryptedUserId = "12345";
        request.setMethod("GET");
        request.setParameter("userId", encryptedUserId);
        when(idEncryptor.decrypt(encryptedUserId)).thenReturn(decryptedUserId);
        assertThrows(NullPointerException.class, ()->requestParameterDecryptionFilter.doFilterInternal(request, response, null));
    }

    @Test
    void doFilterInternal_ShouldDecryptUserId_When_Method_Not_EqualTo_Get() throws ServletException, IOException {
        String encryptedUserId = "encryptedUserId";
        String decryptedUserId = "12345";
        request.setMethod("post");
        request.setParameter("userId", encryptedUserId);
        when(idEncryptor.decrypt(encryptedUserId)).thenReturn(decryptedUserId);
        requestParameterDecryptionFilter.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_ShouldSendBadRequest_WhenDecryptionFails() throws ServletException, IOException {
        request.setMethod("DELETE");
        request.setParameter("userId");
        filterChain.doFilter(request,response);
        requestParameterDecryptionFilter.doFilterInternal(request, response, filterChain);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void doFilterInternal_ShouldProceedWithoutDecryption_WhenUserIdParameterIsAbsent() throws ServletException, IOException {
        request.setMethod("GET");
        requestParameterDecryptionFilter.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
        verify(idEncryptor, never()).decrypt(anyString());
    }

    @Test
    void doFilterInternal_ShouldProceedWithoutDecryption_WhenUserIdParameterIsEmpty() throws ServletException, IOException {
        String encryptedUserId = "";
        String decryptedUserId = "";
        request.setMethod("GET");
        request.setParameter("userId", encryptedUserId);
        when(idEncryptor.decrypt(encryptedUserId)).thenReturn(decryptedUserId);
        requestParameterDecryptionFilter.doFilterInternal(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldNotFilter_ShouldReturn_True_For_NonGetRequests() {
        request.setMethod("POST");
        boolean result = requestParameterDecryptionFilter.shouldNotFilter(request);
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_ShouldReturn_False_For_GetRequests() {
        request.setMethod("GET");
        boolean result = requestParameterDecryptionFilter.shouldNotFilter(request);
        assertFalse(result);
    }

    @Test
    void shouldNotFilter_ShouldReturn_False_For_DeleteRequests() {
        request.setMethod("DELETE");
        boolean result = requestParameterDecryptionFilter.shouldNotFilter(request);
        assertFalse(result);
    }

    @Test
    void shouldNotFilter_Exception() {
        assertThrows(NullPointerException.class, ()->requestParameterDecryptionFilter.shouldNotFilter(null));
    }
}