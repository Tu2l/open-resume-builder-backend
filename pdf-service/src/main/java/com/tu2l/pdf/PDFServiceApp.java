package com.tu2l.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PDFServiceApp {
    private static final Logger logger = LoggerFactory.getLogger(PDFServiceApp.class);
  
    public static void main(String[] args) {
        logger.info("Starting PDF Service Application...");
        SpringApplication.run(PDFServiceApp.class, args);
        logger.info("PDF Service Application started successfully");
    }
}
