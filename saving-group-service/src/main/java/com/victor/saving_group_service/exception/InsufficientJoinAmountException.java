package com.victor.saving_group_service.exception;

public class InsufficientJoinAmountException extends RuntimeException {
    public InsufficientJoinAmountException(String message) {
        super(message);
    }
}
