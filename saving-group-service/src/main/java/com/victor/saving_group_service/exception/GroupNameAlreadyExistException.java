package com.victor.saving_group_service.exception;

public class GroupNameAlreadyExistException extends RuntimeException {
    public GroupNameAlreadyExistException(String message) {
        super(message);
    }
}
