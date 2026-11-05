package org.techpulse.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record ApiError(
        @Schema(description = "Time the error occurred", example = "2026-11-05T12:00:00Z")
        Instant timestamp,
        @Schema(description = "HTTP status code", example = "404")
        int status,
        @Schema(description = "HTTP status reason", example = "Not Found")
        String error,
        @Schema(description = "Details about the failure", example = "WebSeries not found")
        String message,
        @Schema(description = "Request path that caused the error", example = "/api/webseries/89b5b26e-4656-4f2c-8a11-e69637cfa312")
        String path) {
}
