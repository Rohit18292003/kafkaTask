package org.techpulse.producer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EpisodeResponse(
        UUID id,
        UUID seasonId,
        Integer episodeNumber,
        String title,
        Integer durationMinutes,
        LocalDateTime createdAt) {
}
