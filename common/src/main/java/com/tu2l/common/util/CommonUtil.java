package com.tu2l.common.util;

import com.tu2l.common.constant.CommonConstants;

public class CommonUtil {

    public String decodeBase64StringToString(String base64String) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64String);
        return new String(decodedBytes);
    }

    public String decodeAndSanitizeBase64StringToString(String base64String) {
        return sanitizeHtml(decodeBase64StringToString(base64String));
    }

    /**
     * Sanitizes HTML content by removing potentially dangerous tags and attributes.
     * This helps prevent XSS attacks and limits file system access risks.
     *
     * @param html The HTML content to sanitize
     * @return Sanitized HTML content
     */
    public String sanitizeHtml(String html) {
        if (html == null) {
            return null;
        }

        // Remove script tags and their content
        String sanitized = html.replaceAll("(?i)<script[^>]*>.*?</script>", "");

        // Remove potentially dangerous event handlers
        sanitized = sanitized.replaceAll("(?i)\\s*on\\w+\\s*=\\s*['\"][^'\"]*['\"]?", "");

        // Remove javascript: protocol from href and src attributes
        sanitized = sanitized.replaceAll("(?i)(href|src)\\s*=\\s*['\"]?javascript:[^'\"\\s>]*['\"]?", "");

        // Remove file:// protocol to prevent local file access
        sanitized = sanitized.replaceAll("(?i)(href|src)\\s*=\\s*['\"]?file://[^'\"\\s>]*['\"]?", "");

        // Remove data: URLs that could contain malicious content
        sanitized = sanitized.replaceAll("(?i)(href|src)\\s*=\\s*['\"]?data:[^'\"\\s>]*['\"]?", "");

        // Remove iframe tags
        sanitized = sanitized.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        sanitized = sanitized.replaceAll("(?i)<iframe[^>]*/?>", "");

        // Remove object and embed tags
        sanitized = sanitized.replaceAll("(?i)<(object|embed)[^>]*>.*?</(object|embed)>", "");
        sanitized = sanitized.replaceAll("(?i)<(object|embed)[^>]*/?>", "");

        return sanitized;
    }

    public String encodeByteArrayToBase64String(byte[] input) {
        byte[] encodedBytes = java.util.Base64.getEncoder().encode(input);
        return new String(encodedBytes);
    }

    public String cleanExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    public boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public boolean isValidEmail(String email) {
        return !isNullOrEmpty(email) && email.matches(CommonConstants.Pattern.EMAIL);
    }

    /**
     * Computes a deterministic SHA-256 hash of the given value and returns it as a
     * lowercase hex string (64 chars). Used to store opaque tokens (refresh/reset/
     * verification) at rest instead of plaintext, while still allowing direct
     * lookups by hashing the incoming token and comparing.
     *
     * @param value the value to hash (e.g. a JWT)
     * @return lowercase hex-encoded SHA-256 digest, or {@code null} if value is null
     */
    public String sha256Hex(String value) {
        if (value == null) {
            return null;
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every JVM.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

}
