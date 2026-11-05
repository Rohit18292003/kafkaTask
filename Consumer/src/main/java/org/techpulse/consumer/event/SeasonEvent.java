package org.techpulse.consumer.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record SeasonEvent(
        @NotNull UUID id,
        @NotNull UUID webSeriesId,
        @NotNull @Positive Integer seasonNumber,
        @NotBlank String title) {
}
