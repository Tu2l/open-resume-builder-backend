package com.tu2l.pdf.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PDFServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(PDFServiceApp.class, args);
    }
}
