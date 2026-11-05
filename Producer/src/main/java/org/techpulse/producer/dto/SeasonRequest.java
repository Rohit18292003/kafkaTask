package org.techpulse.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record SeasonRequest(
        @Schema(description = "UUID of the parent web series",
                example = "89b5b26e-4656-4f2c-8a11-e69637cfa312",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID webSeriesId,
        @Schema(description = "Positive season number within the web series", example = "1",
                minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer seasonNumber,
        @Schema(description = "Season title", example = "One", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String title) {
}
