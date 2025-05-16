package com.victor.saving_group_service.exception;

public class AlreadyGroupMemberException extends RuntimeException {
    public AlreadyGroupMemberException(String message) {
        super(message);
    }
}
