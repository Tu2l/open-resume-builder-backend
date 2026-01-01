package com.tu2l.user.service.impl;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.service.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PasswordServiceImpl implements PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final CommonUtil commonUtil;

    @Override
    public String hashPassword(String base64EncodedPassword) {
        return passwordEncoder.encode(commonUtil.decodeBase64StringToString(base64EncodedPassword));
    }

    @Override
    public boolean verifyPassword(String base64EncodedPassword, String hashedPassword) {
        return passwordEncoder.matches(commonUtil.decodeBase64StringToString(base64EncodedPassword), hashedPassword);
    }
}
