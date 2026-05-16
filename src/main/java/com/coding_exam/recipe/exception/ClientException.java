package com.coding_exam.recipe.exception;

import com.coding_exam.recipe.exception.type.ErrorCode;

public class ClientException extends RuntimeException {

    private final String errorCode;

    public ClientException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}   