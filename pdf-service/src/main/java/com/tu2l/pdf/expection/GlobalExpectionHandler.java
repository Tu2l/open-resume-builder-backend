package com.tu2l.pdf.expection;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tu2l.common.models.base.BaseResponse;
import com.tu2l.common.models.states.ResponseProcessingStatus;

@RestControllerAdvice
public class GlobalExpectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExpectionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGlobalExpections(Exception exception) {
        logger.error("Exception caught", exception);

        BaseResponse error = new BaseResponse() {};
        error.setMessage(exception.getMessage());
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        logger.warn("Validation failed: {}", errorMessage);

        BaseResponse error = new BaseResponse() {};
        error.setMessage(errorMessage);
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}