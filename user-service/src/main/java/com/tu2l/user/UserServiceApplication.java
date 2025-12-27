package com.tu2l.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        log.info("Starting UMS Application...");
        SpringApplication.run(UserServiceApplication.class, args);
        log.info("UMS started successfully");
    }
}