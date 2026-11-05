package org.techpulse.producer.event;

import java.util.UUID;

public record WebSeriesEvent(
        UUID id,
        String title,
        String description,
        Integer releaseYear) {
}
