package com.victor.saving_group_service.exception;

public class InvalidPayoutOrderException extends RuntimeException {
    public InvalidPayoutOrderException(String message) {
        super(message);
    }
}
