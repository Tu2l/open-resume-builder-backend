package com.tu2l.gateway;

import com.tu2l.gateway.config.CustomGatewayProperties;
import com.tu2l.gateway.config.JwtGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({CustomGatewayProperties.class, JwtGatewayProperties.class})
public class GatewayServiceApplication {

    public static void main(String[] args) {
        log.info("Starting Gateway Service Application...");
        SpringApplication.run(GatewayServiceApplication.class, args);
        log.info("Gateway Service Application started successfully.");
    }
}
