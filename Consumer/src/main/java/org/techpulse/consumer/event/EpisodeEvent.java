package org.techpulse.consumer.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record EpisodeEvent(
        @NotNull UUID id,
        @NotNull UUID seasonId,
        @NotNull @Positive Integer episodeNumber,
        @NotBlank String title,
        @NotNull @Positive Integer durationMinutes) {
}
