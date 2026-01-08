package com.ducknife.project.common.exception;

public class ResourceConflictException extends AppException{
    public ResourceConflictException(String message) {
        super(409, message);
    }
}
