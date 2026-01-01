package com.tu2l.user.service;

public interface PasswordService {
    String hashPassword(String base64EncodedPassword);

    boolean verifyPassword(String base64EncodedPassword, String hashedPassword);
}
