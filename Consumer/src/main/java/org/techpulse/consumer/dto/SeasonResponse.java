package org.techpulse.consumer.dto;

import java.time.Instant;
import java.util.UUID;

public record SeasonResponse(
        UUID id, UUID webSeriesId, Integer seasonNumber, String title, Instant receivedAt, Instant updatedAt) {
}
