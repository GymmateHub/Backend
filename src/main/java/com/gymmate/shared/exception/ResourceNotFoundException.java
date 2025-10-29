package com.gymmate.shared.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String resourceName, String id) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with id: %s", resourceName, id));
    }
}
