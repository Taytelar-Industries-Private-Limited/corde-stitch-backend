package com.cordestitch.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestParameterDecryptionFilter extends OncePerRequestFilter {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws java.io.IOException, ServletException {

        if (GET.equalsIgnoreCase(request.getMethod()) || DELETE.equalsIgnoreCase(request.getMethod())) {
            try {
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                log.error("Error processing request", e);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameters");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !(GET.equalsIgnoreCase(request.getMethod()) || DELETE.equalsIgnoreCase(request.getMethod()));
    }
}
