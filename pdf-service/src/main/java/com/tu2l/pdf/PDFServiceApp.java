package com.tu2l.pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableAsync
public class PDFServiceApp {
  
    public static void main(String[] args) {
        log.info("Starting PDF Service Application...");
        SpringApplication.run(PDFServiceApp.class, args);
        log.info("PDF Service Application started successfully");
    }
}
