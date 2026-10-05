package com.gymmate.notification.internal.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Real-time e-mail delivery status (SSE) for registration/OTP e-mails. The route is
 * unchanged from when it lived on identity's AuthController.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class EmailStatusStreamController {

    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * SSE endpoint for real-time email delivery status.
     * The client subscribes after initiating registration or OTP resend
     * and receives events: SENDING → SENT or FAILED.
     */
    @GetMapping(value = "/email-status/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Email status stream", description = "Subscribe to real-time email delivery status updates")
    public SseEmitter streamEmailStatus(@PathVariable String userId) {
        return sseEmitterRegistry.createEmailStatusEmitter(userId);
    }
}
