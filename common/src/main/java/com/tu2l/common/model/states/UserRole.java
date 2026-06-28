package com.tu2l.common.model.states;

public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN"),
    MODERATOR("MODERATOR"),
    GUEST("GUEST");

    final String value;

    UserRole(String value) {
        this.value = value;
    }

    public static boolean isValid(String role) {
        return switch (role) {
            case "USER", "ADMIN", "MODERATOR", "GUEST" -> true;
            default -> false;
        };
    }
}