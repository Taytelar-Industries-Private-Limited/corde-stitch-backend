package com.cordestitch.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class RequestDecryptionAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;

    public RequestDecryptionAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public @NonNull HttpInputMessage beforeBodyRead(final @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            String originalBody = new String(inputMessage.getBody().readAllBytes());
            log.info("Original request body: {}", originalBody);

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            String userId = (String) request.getAttribute("userId");
            log.info("Extracted userId from request attributes: {}", userId);

            final String modifiedBody = processRequestBody(originalBody,userId);
            log.info("Modified request body: {}", modifiedBody);

            return new HttpInputMessage() {
                @Override
                public @NonNull InputStream getBody() {
                    return new ByteArrayInputStream(modifiedBody.getBytes());
                }

                @Override
                public @NonNull HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };

        } catch (Exception e) {
            log.error("Error processing request body", e);
            throw new IOException("Error processing request body", e);
        }
    }

    private String processRequestBody(String originalBody, String userId) throws Exception {
        log.info("Processing request body : {}", originalBody);
        if (originalBody == null || originalBody.isEmpty()) {
            return originalBody;
        }

        Map<String, Object> bodyMap = objectMapper.readValue(originalBody, new TypeReference<>() {});
        if (userId != null && !userId.isEmpty()) {
            bodyMap.put("userId", userId);
        }

        return objectMapper.writeValueAsString(bodyMap);
    }
}