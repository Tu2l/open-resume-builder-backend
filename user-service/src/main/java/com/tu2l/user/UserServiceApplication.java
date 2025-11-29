package com.tu2l.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        log.info("Starting User Service Application...");
        SpringApplication.run(UserServiceApplication.class, args);
        log.info("User Service Application started successfully");
    }
}