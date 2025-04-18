package com.cordestitch.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestDecryptionAdviceTest {
    @InjectMocks
    private RequestDecryptionAdvice requestDecryptionAdvice;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void supports_Exception_When_MethodParameter_Is_Null(){
        Type type = mock(Type.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.supports(null,type,converterType));
    }

    @Test
    void supports_Exception_When_Type_Is_Null(){
        MethodParameter methodParameter = mock(MethodParameter.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.supports(methodParameter,null,converterType));
    }

    @Test
    void supports_Exception_When_ConverterType_Is_Null(){
        MethodParameter methodParameter = mock(MethodParameter.class);
        Type type = mock(Type.class);
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.supports(methodParameter,type,null));
    }

    @Test
    void beforeBodyRead_Exception_When_InputMessage_Is_NUll(){
        MethodParameter methodParameter = mock(MethodParameter.class);
        Type type = mock(Type.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.beforeBodyRead(null, methodParameter, type, converterType));
    }

    @Test
    void beforeBodyRead_Exception_When_MethodParameter_Is_NUll(){
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        Type type = mock(Type.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.beforeBodyRead(inputMessage, null, type, converterType));
    }
    @Test
    void beforeBodyRead_Exception_When_Type_Is_NUll(){
        MethodParameter methodParameter = mock(MethodParameter.class);
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.beforeBodyRead(inputMessage, methodParameter, null, converterType));
    }
    @Test
    void beforeBodyRead_Exception_When_ConverterType_Is_NUll(){
        MethodParameter methodParameter = mock(MethodParameter.class);
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        Type type = mock(Type.class);
        assertThrows(NullPointerException.class, ()->requestDecryptionAdvice.beforeBodyRead(inputMessage, methodParameter, type, null));
    }
    @Test
    void beforeBodyRead_Exception_Error_Processing_Request_Body() {
        MethodParameter methodParameter = mock(MethodParameter.class);
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        Type type = mock(Type.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;
        request.setAttribute("",Object.class);
        assertThrows(IOException.class, ()->requestDecryptionAdvice.beforeBodyRead(inputMessage, methodParameter, type, converterType));
    }

    @Test
    void beforeBodyRead_When_OriginalBody_Is_Null() throws IOException {
        MethodParameter methodParameter = mock(MethodParameter.class);
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);
        Type type = mock(Type.class);
        @SuppressWarnings("unchecked")
        Class<? extends HttpMessageConverter<?>> converterType = (Class<? extends HttpMessageConverter<?>>) (Class<?>) HttpMessageConverter.class;

        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        String userId = "user123";
        when(request.getAttribute("userId")).thenReturn(userId);

        String originalBody = "";
        InputStream inputStream = new ByteArrayInputStream(originalBody.getBytes());
        when(inputMessage.getBody()).thenReturn(inputStream);

        requestDecryptionAdvice = new RequestDecryptionAdvice(objectMapper);

        HttpInputMessage result = requestDecryptionAdvice.beforeBodyRead(inputMessage, methodParameter, type, converterType);

        assertNotNull(result);
        assertEquals("", new String(result.getBody().readAllBytes()));
        verify(request, times(1)).getAttribute("userId");
    }

}