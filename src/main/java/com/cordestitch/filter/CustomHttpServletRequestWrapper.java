package com.cordestitch.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Map;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> modifiedParameters;

    public CustomHttpServletRequestWrapper(HttpServletRequest request, Map<String, String[]> modifiedParameters) {
        super(request);
        this.modifiedParameters = modifiedParameters;
    }

    @Override
    public String getParameter(String name) {
        String[] params = modifiedParameters.get(name);
        return (params != null && params.length > 0) ? params[0] : super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return modifiedParameters;
    }

    @Override
    public String[] getParameterValues(String name) {
        return modifiedParameters.getOrDefault(name, super.getParameterValues(name));
    }
}

