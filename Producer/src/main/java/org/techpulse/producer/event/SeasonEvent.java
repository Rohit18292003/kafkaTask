package org.techpulse.producer.event;

import java.util.UUID;

public record SeasonEvent(
        UUID id,
        UUID webSeriesId,
        Integer seasonNumber,
        String title) {
}
