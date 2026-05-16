package com.coding_exam.recipe.exception;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import com.coding_exam.recipe.exception.type.ErrorCode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {
    
    private static final String ERROR_CODE_KEY = "error-code";
    private static final String ERROR_MESSAGE_KEY = "error-message";

    private static final Map<String, HttpStatus> errorCodeToHttpStatusCodeMap = Map.of(
        ErrorCode.REQUEST_ALREADY_PROCESSED, HttpStatus.UNPROCESSABLE_ENTITY,
        ErrorCode.VALIDATION_EXCEPTION, HttpStatus.BAD_REQUEST,
        ErrorCode.GENERIC_EXCEPTION, HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCode.RECIPE_NOT_FOUND, HttpStatus.NOT_FOUND
    );

    @ExceptionHandler(value = {ClientException.class})
    public void clientException(ClientException ex, HttpServletResponse response) {
        log.error("Client exception", ex);
        response.addHeader(ERROR_CODE_KEY, ex.getErrorCode());
        response.addHeader(ERROR_MESSAGE_KEY, ex.getMessage());
        response.setStatus(errorCodeToHttpStatusCodeMap.get(ex.getErrorCode()).value());
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, MethodArgumentNotValidException.class, 
                               MissingRequestHeaderException.class, MissingPathVariableException.class, 
                               DataIntegrityViolationException.class, MismatchedInputException.class})
    public void validationException(Exception ex, HttpServletResponse response) {
        log.error("Validation exception", ex);
        response.addHeader(ERROR_CODE_KEY, ErrorCode.VALIDATION_EXCEPTION);
        response.addHeader(ERROR_MESSAGE_KEY, ex.getMessage());
        response.setStatus(errorCodeToHttpStatusCodeMap.get(ErrorCode.VALIDATION_EXCEPTION).value());
    }

    @ExceptionHandler(value = {NoResourceFoundException.class})
    public void noResourceFoundException(ServletException ex, HttpServletResponse response) {
        log.error("No resource found exception", ex);
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public void httpMethodViolationException(HttpRequestMethodNotSupportedException ex, HttpServletResponse response) {
        log.error("HTTP method violation exception", ex);
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @ExceptionHandler(value = {Exception.class})
    public void internalServerError(Exception ex, HttpServletResponse response) {
        log.error("General Exception: ", ex);
        response.addHeader(ERROR_CODE_KEY, ErrorCode.GENERIC_EXCEPTION);
        response.addHeader(ERROR_MESSAGE_KEY, ex.getMessage());
        response.setStatus(errorCodeToHttpStatusCodeMap.get(ErrorCode.GENERIC_EXCEPTION).value());
    }
}
