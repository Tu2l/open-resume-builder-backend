package com.tu2l.pdf.api.utils;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Util {

    public String decodeBase64StringToString(String base64String) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64String);
        return new String(decodedBytes);
    }

    public String sanitizeHtmlForXhtml(String html) {
        return html
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    public String escapeHtmlEntities(String html) {
        // Only escape standalone & characters, not already escaped entities
        return html.replaceAll("&(?!(amp|lt|gt|quot|apos|#\\d+|#x[0-9a-fA-F]+);)", "&amp;");
    }

    public String convertToXhtml(String html) {
        // Escape unescaped & characters
        String xhtml = escapeHtmlEntities(html);
        
        // Convert self-closing tags to XHTML format
        xhtml = xhtml.replaceAll("<meta([^>]*?)>", "<meta$1/>");
        xhtml = xhtml.replaceAll("<br([^>]*?)>", "<br$1/>");
        xhtml = xhtml.replaceAll("<hr([^>]*?)>", "<hr$1/>");
        xhtml = xhtml.replaceAll("<img([^>]*?)>", "<img$1/>");
        xhtml = xhtml.replaceAll("<input([^>]*?)>", "<input$1/>");
        xhtml = xhtml.replaceAll("<link([^>]*?)>", "<link$1/>");
        xhtml = xhtml.replaceAll("<area([^>]*?)>", "<area$1/>");
        xhtml = xhtml.replaceAll("<base([^>]*?)>", "<base$1/>");
        xhtml = xhtml.replaceAll("<col([^>]*?)>", "<col$1/>");
        xhtml = xhtml.replaceAll("<embed([^>]*?)>", "<embed$1/>");
        xhtml = xhtml.replaceAll("<param([^>]*?)>", "<param$1/>");
        xhtml = xhtml.replaceAll("<source([^>]*?)>", "<source$1/>");
        xhtml = xhtml.replaceAll("<track([^>]*?)>", "<track$1/>");
        xhtml = xhtml.replaceAll("<wbr([^>]*?)>", "<wbr$1/>");
        
        return xhtml;
    }

    public String encodeByteArrayToBase64String(byte[] input) {
        byte[] encodedBytes = java.util.Base64.getEncoder().encode(input);
        return new String(encodedBytes);
    }

}
