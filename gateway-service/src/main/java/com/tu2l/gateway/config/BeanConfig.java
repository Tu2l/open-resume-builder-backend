package com.tu2l.gateway.config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class BeanConfig {
    @Bean
    public WebProperties.Resources webPropertiesResources() {
        return new WebProperties.Resources();
    }

    @Bean
    public ErrorProperties errorProperties() {
        return new ErrorProperties();
    }

    @Bean
    public AntPathMatcher matcher() {
        return new AntPathMatcher();
    }
}
