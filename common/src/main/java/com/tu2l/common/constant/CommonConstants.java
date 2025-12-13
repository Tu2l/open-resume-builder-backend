package com.tu2l.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Common Constants used across all microservices
 * This class contains all shared constants to ensure consistency
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ============ HTTP Headers ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String USER_AGENT = "User-Agent";
        public static final String X_USER_ID = "X-User-Id";
        public static final String X_USER_EMAIL = "X-User-Email";
        public static final String X_USER_ROLE = "X-User-Role";
        public static final String X_REQUEST_ID = "X-Request-Id";
        public static final String X_CORRELATION_ID = "X-Correlation-Id";
    }

    // ============ Content Types ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class ContentType {
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_PDF = "application/pdf";
        public static final String APPLICATION_XML = "application/xml";
        public static final String TEXT_HTML = "text/html";
        public static final String TEXT_PLAIN = "text/plain";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    }

    // ============ Token Constants ============
    public static final class Token {
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String TOKEN_TYPE_BEARER = "Bearer";
        public static final String TOKEN_TYPE_REFRESH = "refresh";
        public static final String TOKEN_TYPE_PASSWORD_RESET = "password_reset";
        public static final String TOKEN_TYPE_EMAIL_VERIFICATION = "email_verification";
        public static final int BEARER_PREFIX_LENGTH = 7;

        private Token() {}
    }

    // ============ JWT Claims ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class JwtClaims {
        public static final String USER_ID = "userId";
        public static final String EMAIL = "email";
        public static final String ROLE = "role";
        public static final String TOKEN_TYPE = "tokenType";
        public static final String SUBJECT = "sub";
        public static final String ISSUER = "iss";
        public static final String ISSUED_AT = "iat";
        public static final String EXPIRATION = "exp";
    }

    // ============ API Path Prefixes ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class ApiPath {
        public static final String API_PREFIX = "/api";
        public static final String USER_API = "/api/user";
        public static final String PDF_API = "/api/pdf";
        public static final String AUTH_PATH = "/auth";
        public static final String AUTHORIZE_PATH = "/authorize";

        // Full paths
        public static final String USER_AUTH = USER_API + AUTH_PATH;
        public static final String USER_AUTHORIZE = USER_API + AUTHORIZE_PATH;
    }

    // ============ Service Context Paths ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class ServiceContext {
        public static final String USER_SERVICE = "/user";
        public static final String PDF_SERVICE = "/pdf";
        public static final String GATEWAY_SERVICE = "/";
    }

    // ============ Validation Constants ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class Validation {
        // Username validation
        public static final int USERNAME_MIN_LENGTH = 3;
        public static final int USERNAME_MAX_LENGTH = 50;
        public static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]+$";

        // Password validation
        public static final int PASSWORD_MIN_LENGTH = 8;
        public static final int PASSWORD_MAX_LENGTH = 100;
        public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$";

        // Email validation - using standard Jakarta Bean Validation @Email

        // Phone number validation
        public static final String PHONE_PATTERN = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,9}$";
    }

    // ============ Validation Messages ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class ValidationMessage {
        // Username messages
        public static final String USERNAME_REQUIRED = "Username is required";
        public static final String USERNAME_SIZE = "Username must be between 3 and 50 characters";
        public static final String USERNAME_PATTERN_MSG = "Username can only contain letters, numbers, dots, underscores and hyphens";

        // Email messages
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String EMAIL_INVALID = "Email must be valid";

        // Password messages
        public static final String PASSWORD_REQUIRED = "Password is required";
        public static final String PASSWORD_SIZE = "Password must be between 8 and 100 characters";
        public static final String PASSWORD_PATTERN_MSG = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character";

        // Token messages
        public static final String TOKEN_REQUIRED = "Token is required";
        public static final String TOKEN_INVALID = "Token is invalid";
    }

    // ============ HTTP Status Messages ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class StatusMessage {
        // Success messages
        public static final String SUCCESS = "Success";
        public static final String CREATED = "Created successfully";
        public static final String UPDATED = "Updated successfully";
        public static final String DELETED = "Deleted successfully";

        // Error messages
        public static final String BAD_REQUEST = "Bad request";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String FORBIDDEN = "Forbidden";
        public static final String NOT_FOUND = "Not found";
        public static final String CONFLICT = "Conflict";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
        public static final String SERVICE_UNAVAILABLE = "Service unavailable";

        // Authentication/Authorization messages
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String ACCOUNT_LOCKED = "Account is locked";
        public static final String ACCOUNT_DISABLED = "Account is disabled";
        public static final String TOKEN_EXPIRED = "Token has expired";
        public static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions";
    }

    // ============ Error Codes ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class ErrorCode {
        // Authentication errors (1xxx)
        public static final String AUTH_INVALID_CREDENTIALS = "AUTH_1001";
        public static final String AUTH_TOKEN_EXPIRED = "AUTH_1002";
        public static final String AUTH_TOKEN_INVALID = "AUTH_1003";
        public static final String AUTH_UNAUTHORIZED = "AUTH_1004";

        // User errors (2xxx)
        public static final String USER_NOT_FOUND = "USER_2001";
        public static final String USER_ALREADY_EXISTS = "USER_2002";
        public static final String USER_INVALID_INPUT = "USER_2003";

        // Resource errors (3xxx)
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_3001";
        public static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_3002";

        // Service errors (5xxx)
        public static final String SERVICE_UNAVAILABLE = "SERVICE_5001";
        public static final String INTERNAL_ERROR = "SERVICE_5002";
    }

    // ============ Date/Time Formats ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class DateTimeFormat {
        public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        public static final String DATE_ONLY = "yyyy-MM-dd";
        public static final String TIME_ONLY = "HH:mm:ss";
        public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
        public static final String DISPLAY_FORMAT = "MMM dd, yyyy HH:mm";
    }

    // ============ Default Values ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class Defaults {
        public static final String DEFAULT_TIMEZONE = "UTC";
        public static final String DEFAULT_LOCALE = "en_US";
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_DIRECTION = "ASC";
        public static final String DEFAULT_USER_ROLE = "ROLE_USER";
    }

    // ============ PDF Generation Constants ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class PDF {
        public static final String DEFAULT_TOP_MARGIN = "6mm";
        public static final String DEFAULT_BOTTOM_MARGIN = "6mm";
        public static final String DEFAULT_LEFT_MARGIN = "8mm";
        public static final String DEFAULT_RIGHT_MARGIN = "8mm";
        public static final String DEFAULT_PAGE_SIZE = "A4";
        public static final String DEFAULT_ORIENTATION = "PORTRAIT";
    }

    // ============ Cache Keys ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class CacheKey {
        public static final String USER_PREFIX = "user:";
        public static final String TOKEN_PREFIX = "token:";
        public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
        public static final String PASSWORD_RESET_PREFIX = "password_reset:";
        public static final String EMAIL_VERIFICATION_PREFIX = "email_verify:";
    }

    // ============ Regex Patterns ============
    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static final class Pattern {
        public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        public static final String ALPHANUMERIC = "^[a-zA-Z0-9]+$";
        public static final String NUMERIC = "^[0-9]+$";
        public static final String URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
    }
}