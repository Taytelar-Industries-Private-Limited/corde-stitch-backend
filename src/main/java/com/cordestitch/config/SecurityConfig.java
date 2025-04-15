package com.cordestitch.config;

import com.cordestitch.filter.JwtRequestFilter;
import com.cordestitch.filter.RequestParameterDecryptionFilter;
import com.cordestitch.util.Constants;
import com.cordestitch.validation.accessdenied.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import static com.cordestitch.util.SecurityConstantsUrls.*;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final JwtRequestFilter jwtRequestFilter;
    private final RequestParameterDecryptionFilter requestParameterDecryptionFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(getWhiteListUrls()).permitAll()
                        .requestMatchers(getAdminUrls()).hasAuthority(Constants.ADMIN)
                        .requestMatchers(getCustomerUrls()).hasAuthority(Constants.CUSTOMER)
                  .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception-> exception.accessDeniedHandler(customAccessDeniedHandler));

        http.addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(requestParameterDecryptionFilter, JwtRequestFilter.class);

        return http.build();
    }
}

