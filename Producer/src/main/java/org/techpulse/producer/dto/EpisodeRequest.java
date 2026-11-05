package org.techpulse.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record EpisodeRequest(
        @Schema(description = "UUID of the parent season",
                example = "89b5b26e-4656-4f2c-8a11-e69637cfa312",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID seasonId,
        @Schema(description = "Positive episode number within the season", example = "1",
                minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer episodeNumber,
        @Schema(description = "Episode title", example = "Dulcinea",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String title,
        @Schema(description = "Episode runtime in minutes", example = "43",
                minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer durationMinutes) {
}
