package com.tu2l.gateway.exception;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.autoconfigure.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GlobalExceptionHandler extends DefaultErrorWebExceptionHandler {

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes    the error attributes
     * @param resources          the resources configuration properties
     * @param errorProperties    the error configuration properties
     * @param applicationContext the current application context
     * @since 2.4.0
     */
    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties.Resources resources,
                                  ErrorProperties errorProperties,
                                  ApplicationContext applicationContext,
                                  ObjectProvider<@NonNull ViewResolver> viewResolvers,
                                  ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
    }


    @NonNull
    @Override
    protected RouterFunction<@NonNull ServerResponse> getRoutingFunction(@NonNull ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @NonNull
    @Override
    protected Mono<@NonNull ServerResponse> renderErrorResponse(@NonNull ServerRequest request) {
        ErrorAttributeOptions options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE);
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, options);
        return ServerResponse.status(Objects.requireNonNull(HttpStatus.resolve((Integer) errorPropertiesMap.get("status"))))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }

}
