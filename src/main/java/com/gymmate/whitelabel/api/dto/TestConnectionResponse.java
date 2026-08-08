package com.gymmate.whitelabel.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionResponse {

    private boolean success;
    private String message;
    private String details;

    public static TestConnectionResponse success(String message) {
        return TestConnectionResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static TestConnectionResponse failure(String message, String details) {
        return TestConnectionResponse.builder()
                .success(false)
                .message(message)
                .details(details)
                .build();
    }
}
