package org.techpulse.producer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SeasonResponse(
        UUID id,
        UUID webSeriesId,
        Integer seasonNumber,
        String title,
        LocalDateTime createdAt) {
}
