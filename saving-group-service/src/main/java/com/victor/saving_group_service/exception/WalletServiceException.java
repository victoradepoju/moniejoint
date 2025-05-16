package com.victor.saving_group_service.exception;

import org.springframework.http.HttpStatus;

public class WalletServiceException extends RuntimeException {
    private final HttpStatus statusCode;

    public WalletServiceException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public WalletServiceException(String message, HttpStatus statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
