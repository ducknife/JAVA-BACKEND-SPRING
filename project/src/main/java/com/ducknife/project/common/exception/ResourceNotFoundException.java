package com.ducknife.project.common.exception;

public class ResourceNotFoundException extends AppException{
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
