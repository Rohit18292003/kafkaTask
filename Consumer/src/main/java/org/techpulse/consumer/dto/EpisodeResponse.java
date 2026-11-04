package org.techpulse.consumer.dto;

import java.time.Instant;
import java.util.UUID;

public record EpisodeResponse(
        UUID id,
        UUID seasonId,
        Integer episodeNumber,
        String title,
        Integer durationMinutes,
        Instant receivedAt,
        Instant updatedAt) {
}
