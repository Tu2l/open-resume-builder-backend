package com.tu2l.user.service;

public interface EmailService {
    boolean sendVerificationEmail(String to, String payload);

    boolean sendPasswordResetEmail(String to, String payload);
}
