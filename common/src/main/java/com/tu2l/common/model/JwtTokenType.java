package com.tu2l.common.model;

public enum JwtTokenType {
    ACCESS,
    REFRESH,
    PASSWORD_RESET,
    EMAIL_VERIFICATION;

    private final String lowerCaseValue;

    JwtTokenType() {
        this.lowerCaseValue = this.name().toLowerCase();
    }

    public String getLowerCaseValue() {
        return lowerCaseValue;
    }
}
