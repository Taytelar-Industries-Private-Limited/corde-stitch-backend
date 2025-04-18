package com.cordestitch.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomHttpServletRequestWrapperTest {
    private HttpServletRequest mockRequest;
    private Map<String, String[]> modifiedParams;
    private CustomHttpServletRequestWrapper customRequestWrapper;

    @BeforeEach
    void setUp() {
        mockRequest = mock(HttpServletRequest.class);
        modifiedParams = new HashMap<>();
        modifiedParams.put("name", new String[]{"John"});
        modifiedParams.put("age", new String[]{"30"});
        customRequestWrapper = new CustomHttpServletRequestWrapper(mockRequest, modifiedParams);
    }

    @Test
    void testGetParameterReturnsModifiedParameter() {
        String result = customRequestWrapper.getParameter("name");
        assertEquals("John", result);
    }

    @Test
    void testGetParameterReturnsOriginalWhenNotModified() {
        when(mockRequest.getParameter("nonexistent")).thenReturn("Original");
        String result = customRequestWrapper.getParameter("nonexistent");
        assertEquals("Original", result);
    }

    @Test
    void testGetParameterReturnsModifiedParameter_When_Params_Length_Is_LessThan_0() {
        modifiedParams.put("name", new String[]{});
        String result = customRequestWrapper.getParameter("name");
        assertEquals(null, result);
    }

    @Test
    void testGetParameterMapReturnsModifiedParameters() {
        Map<String, String[]> result = customRequestWrapper.getParameterMap();
        assertEquals(modifiedParams, result);
    }

    @Test
    void testGetParameterValuesReturnsModifiedValues() {
        String[] result = customRequestWrapper.getParameterValues("name");
        assertArrayEquals(new String[]{"John"}, result);
    }

}