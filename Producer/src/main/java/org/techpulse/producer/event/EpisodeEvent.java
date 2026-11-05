package org.techpulse.producer.event;

import java.util.UUID;

public record EpisodeEvent(
        UUID id,
        UUID seasonId,
        Integer episodeNumber,
        String title,
        Integer durationMinutes) {
}
