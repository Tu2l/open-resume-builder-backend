package com.tu2l.common.model;

import com.tu2l.common.constant.CommonConstants;
import lombok.Getter;

@Getter
public enum JwtTokenType {
    ACCESS(CommonConstants.Token.TOKEN_TYPE_BEARER),
    REFRESH(CommonConstants.Token.TOKEN_TYPE_REFRESH),
    PASSWORD_RESET(CommonConstants.Token.TOKEN_TYPE_PASSWORD_RESET),
    EMAIL_VERIFICATION(CommonConstants.Token.TOKEN_TYPE_EMAIL_VERIFICATION);

    private final String value;

    JwtTokenType(String value) {
        this.value = value;
    }

}
