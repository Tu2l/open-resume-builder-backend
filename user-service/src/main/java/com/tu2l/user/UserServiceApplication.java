package com.tu2l.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class UserServiceApplication {
    public static void main(String[] args) {
        log.info("Starting UMS Application...");
        SpringApplication.run(UserServiceApplication.class, args);
        log.info("UMS started successfully");
    }
}