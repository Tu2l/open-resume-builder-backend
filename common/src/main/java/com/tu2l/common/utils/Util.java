package com.tu2l.common.utils;

public class Util {

    public String decodeBase64StringToString(String base64String) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64String);
        return new String(decodedBytes);
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

}
