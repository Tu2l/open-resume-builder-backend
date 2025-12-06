package com.tu2l.user.service.impl;

import org.springframework.stereotype.Service;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.service.JwtService;

@Service
public class JwtServiceImpl implements JwtService {

    @Override
    public String generateToken(UserEntity user, JwtTokenType tokenType) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateToken'");
    }

    @Override
    public Boolean validateToken(String token, JwtTokenType tokenType) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateToken'");
    }

    @Override
    public String refreshAccessToken(String refreshToken) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshAccessToken'");
    }

    @Override
    public String getUsername(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUsername'");
    }

    @Override
    public Long getUserId(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserId'");
    }

    @Override
    public Boolean verifyRole(String token, UserRole role) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyRole'");
    }

}
