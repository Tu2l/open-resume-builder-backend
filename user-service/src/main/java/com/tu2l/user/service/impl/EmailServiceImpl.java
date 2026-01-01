package com.tu2l.user.service.impl;

import com.tu2l.user.service.EmailService;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Override
    public boolean sendVerificationEmail(String to, String payload) {
        return false;
    }

    @Override
    public boolean sendPasswordResetEmail(String to, String payload) {
        return false;
    }
}
