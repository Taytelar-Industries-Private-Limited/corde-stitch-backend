package com.cordestitch.config;

import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.filter.RequestDecryptionAdvice;
import com.cordestitch.filter.RequestParameterDecryptionFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<RequestParameterDecryptionFilter> requestDecryptionFilter(
            RequestParameterDecryptionFilter filter) {
        FilterRegistrationBean<RequestParameterDecryptionFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);

        return registrationBean;
    }


    @Bean
    public RequestDecryptionAdvice requestDecryptionAdvice(ObjectMapper objectMapper) {
        return new RequestDecryptionAdvice(objectMapper);
    }

    @Bean
    public IdEncryptor idEncryptor() {
        return new IdEncryptor();
    }
}

