package com.tu2l.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.user.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<BaseResponse> registerUser() throws Exception {
        log.info("Registering new user...");
        // TODO Registration logic here
        BaseResponse response = new BaseResponse() {};
        response.setMessage("User registered successfully");
        response.setStatus(null); // Set appropriate status
        log.info("User registration completed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse> loginUser() throws Exception {
        log.info("User login attempt...");
        // TODO Login logic here
        BaseResponse response = new BaseResponse() {}; 
        response.setMessage("User logged in successfully");
        response.setStatus(null); // Set appropriate status
        log.info("User login completed successfully");
        return ResponseEntity.ok(response);
    }

}
