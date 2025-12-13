package com.tu2l.gateway.exception;

import com.tu2l.common.exception.AuthenticationException;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {
    private final static Logger logger = Logger.getLogger(GlobalErrorAttributes.class.getName());

    @NonNull
    @Override
    public Map<String, Object> getErrorAttributes(@NonNull ServerRequest request, ErrorAttributeOptions options) {
        Throwable throwable = getError(request);
        logger.warning(() -> "Gateway error for %s %s: %s".formatted(request.method(), request.uri(), throwable.getMessage()));

        ErrorAttributeOptions sanitizedOptions = options.excluding(ErrorAttributeOptions.Include.STACK_TRACE);
        Map<String, Object> attributes = super.getErrorAttributes(request, sanitizedOptions);
        attributes.remove("trace");

        if (throwable instanceof ConnectException) {
            attributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            attributes.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
            attributes.put("message", "Downstream service is unavailable. Please try again later.");
        } else if (throwable instanceof UnknownHostException ||
                   (throwable.getCause() != null && throwable.getCause().getClass().getName().contains("DnsErrorCauseException"))) {
            attributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            attributes.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
            attributes.put("message", "Service endpoint could not be resolved. Please check service configuration.");
        } else if (throwable instanceof AuthenticationException) {
            attributes.put("status", HttpStatus.UNAUTHORIZED.value());
            attributes.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
            attributes.put("message", throwable.getMessage());
        } else {
            attributes.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            attributes.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            attributes.put("message", "An unexpected error occurred. Please contact support.");
        }
        return attributes;
    }
}
