package org.techpulse.consumer.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public record WebSeriesEvent(
        @NotNull UUID id,
        @NotBlank String title,
        String description,
        @NotNull @Min(1800) @Max(9999) Integer releaseYear) {
}
