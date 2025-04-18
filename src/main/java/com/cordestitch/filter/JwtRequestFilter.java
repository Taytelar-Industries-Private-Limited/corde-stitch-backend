package com.cordestitch.filter;

import com.cordestitch.exception.token.JwtProcessingException;
import com.cordestitch.service.service.token.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.cordestitch.util.SecurityConstantsUrls.getWhiteListUrls;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final IdEncryptor idEncryptor;
    private static final String SLASH = "/";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("Request received for path: {}", path);

        if (isWhiteListed(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("Auth token: {}", token);

            try {
                boolean valid = jwtService.isTokenValid(token);
                log.info("Token valid: {}", valid);

                if (valid) {
                    String userId = jwtService.extractEncryptedUserId(token);
                    log.info("User id from the token: {}", userId);

                    String decryptedUserId = idEncryptor.decrypt(userId);
                    log.info("Decrypted user id: {}", decryptedUserId);
                    request.setAttribute("userId", decryptedUserId);

                    String role = jwtService.extractUserType(token);
                    log.info("Role from token: {}", role);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            decryptedUserId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("JWT validation successful, proceeding to controller");
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("Authentication failed - sending 401 response");
                    sendUnauthorizedResponse(response, "Invalid or expired token.");
                }
            } catch (JwtProcessingException e) {
                log.error("Error processing JWT token: {}", e.getMessage());
                sendUnauthorizedResponse(response, "Invalid or expired token.");
            }
            return;
        }
        log.warn("Authentication Missing or Malformed - sending 401 response");
        sendUnauthorizedResponse(response, "Authorization token is missing or malformed.");
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean isWhiteListed = isWhiteListed(path);
        if (isWhiteListed && !SLASH.equals(path)) {
            log.info("Path '{}' is white-listed, skipping filter.", path);
        }
        return isWhiteListed;
    }

    private boolean isWhiteListed(String path) {
        for (String whiteListedPath : getWhiteListUrls()) {
            if (path.equals(whiteListedPath)) {
                if (!SLASH.equals(whiteListedPath)) {
                    log.info("Path '{}' is white-listed", path);
                }
                return true;
            }
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String jsonResponse = "{ \"error\": \"Unauthorized\", \"message\": \"" + message + "\" }";
        response.getWriter().write(jsonResponse);
    }
}
