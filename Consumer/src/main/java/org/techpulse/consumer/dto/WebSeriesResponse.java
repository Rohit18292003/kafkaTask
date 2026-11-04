package org.techpulse.consumer.dto;

import java.time.Instant;
import java.util.UUID;

public record WebSeriesResponse(
        UUID id, String title, String description, Integer releaseYear, Instant receivedAt, Instant updatedAt) {
}
