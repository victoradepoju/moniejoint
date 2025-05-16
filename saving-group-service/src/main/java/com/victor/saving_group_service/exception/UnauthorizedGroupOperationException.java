package com.victor.saving_group_service.exception;

public class UnauthorizedGroupOperationException extends RuntimeException {
    public UnauthorizedGroupOperationException(String message) {
        super(message);
    }
}
