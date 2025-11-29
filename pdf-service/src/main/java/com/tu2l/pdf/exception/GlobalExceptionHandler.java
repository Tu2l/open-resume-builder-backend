package com.tu2l.pdf.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tu2l.common.models.base.BaseResponse;
import com.tu2l.common.models.states.ResponseProcessingStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGlobalExceptions(Exception exception) {
        logger.error("Exception caught", exception);

        BaseResponse error = new BaseResponse() {};
        error.setMessage(exception.getMessage());
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
        logger.warn("Resource not found: {}", exception.getMessage());

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
        
        logger.warn("Validation failed: {}", errorMessage);

        BaseResponse error = new BaseResponse() {};
        error.setMessage(errorMessage);
        error.setStatus(ResponseProcessingStatus.FAILURE);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PDFException.class)
    public ResponseEntity<BaseResponse> handlePDFExceptions(PDFException exception) {
        logger.error("PDFException caught: {}", exception.getMessage());
        BaseResponse error = new BaseResponse() {};
        error.setMessage(exception.getMessage());
        error.setStatus(ResponseProcessingStatus.FAILURE);  
        return new ResponseEntity<>(error, HttpStatus.OK);
    }   
}