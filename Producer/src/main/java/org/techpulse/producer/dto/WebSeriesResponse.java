package org.techpulse.producer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WebSeriesResponse(
        UUID id,
        String title,
        String description,
        Integer releaseYear,
        LocalDateTime createdAt) {
}
