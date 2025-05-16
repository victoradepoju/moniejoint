package com.victor.saving_group_service.exception;

public class GroupFullException extends RuntimeException {
    public GroupFullException(String message) {
        super(message);
    }
}
