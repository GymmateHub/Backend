package com.gymmate.whitelabel.internal.application.dto;
public record TestConnectionResponse(
        boolean success,
        String message,
        String details
) {

    public static TestConnectionResponse success(String message) {
        return new TestConnectionResponse(true, message, null);
    }

    public static TestConnectionResponse failure(String message, String details) {
        return new TestConnectionResponse(false, message, details);
    }
}
