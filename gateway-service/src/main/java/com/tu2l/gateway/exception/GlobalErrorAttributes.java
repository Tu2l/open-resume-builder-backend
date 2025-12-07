package com.tu2l.gateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.ConnectException;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    private final static Logger logger = Logger.getLogger(GlobalErrorAttributes.class.getName());

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable throwable = getError(request);
        logger.warning(() -> "Gateway error for %s %s: %s".formatted(request.method(), request.uri(), throwable.getMessage()));

        ErrorAttributeOptions sanitizedOptions = options.excluding(ErrorAttributeOptions.Include.STACK_TRACE);
        Map<String, Object> attributes = super.getErrorAttributes(request, sanitizedOptions);
        attributes.remove("trace");

        if (throwable instanceof ConnectException) {
            attributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            attributes.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
            attributes.put("message", "Downstream service is unavailable. Please try again later.");
        }
        return attributes;
    }
}
