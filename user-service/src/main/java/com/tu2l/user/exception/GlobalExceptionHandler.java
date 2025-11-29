package com.tu2l.user.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGlobalExceptions(Exception exception) {
        log.error("Exception caught", exception);

        BaseResponse error = new BaseResponse() {};
        error.setMessage(exception.getMessage());
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        log.warn("Resource not found: {}", exception.getMessage());

        BaseResponse error = new BaseResponse() {};
        error.setMessage(exception.getMessage());
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", errorMessage);

        BaseResponse error = new BaseResponse() {};
        error.setMessage(errorMessage);
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserException.class, AuthenticationException.class})
    public ResponseEntity<BaseResponse> handleAppLevelExceptions(RuntimeException exception) {
        log.error("UserException caught: {}", exception.getMessage());
        BaseResponse error = new BaseResponse() {};
        error.setMessage(exception.getMessage());
        error.setStatus(ResponseProcessingStatus.FAILURE);  
        return new ResponseEntity<>(error, HttpStatus.OK);
    }   
}