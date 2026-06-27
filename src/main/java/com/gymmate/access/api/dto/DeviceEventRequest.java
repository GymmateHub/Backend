package com.gymmate.access.api.dto;

/**
 * Inbound event from a turnstile / camera (CV) device for pass-count
 * reconciliation. {@code passCount} is the people detected passing through for
 * the entry window; {@code validScanCount} is the number of valid credential
 * scans. When passes exceed valid scans, tailgating is flagged.
 */
public record DeviceEventRequest(
    Integer validScanCount,
    Integer passCount,
    String capturedImageUrl,
    String note
) {
}
