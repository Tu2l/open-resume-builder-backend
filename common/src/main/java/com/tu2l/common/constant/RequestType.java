package com.tu2l.common.constant;

/**
 * Represents the type of request for authentication purposes.
 */
public enum RequestType {
    /**
     * Public request that does not require authentication
     */
    PUBLIC,

    /**
     * Protected request that requires authentication
     */
    PROTECTED,

    /**
     * Undefined or invalid request type
     */
    UNDEFINED;

    /**
     * Parses a string value to a RequestType enum.
     * Returns UNDEFINED for null, empty, or invalid values.
     *
     * @param value the string value to parse (case-insensitive)
     * @return the corresponding RequestType, or UNDEFINED if invalid
     */
    public static RequestType parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNDEFINED;
        }

        try {
            return RequestType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNDEFINED;
        }
    }
}
