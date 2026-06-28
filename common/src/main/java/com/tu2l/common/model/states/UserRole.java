package com.tu2l.common.model.states;

public enum UserRole {
    USER ("USER"),
    ADMIN ("ADMIN"),
    MODERATOR ("MODERATOR"),
    GUEST ("GUEST");

    final String value;

    UserRole(String value) {
        this.value = value;
    }
}