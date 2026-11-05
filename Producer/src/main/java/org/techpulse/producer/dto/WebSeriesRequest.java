package org.techpulse.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WebSeriesRequest(
        @Schema(description = "Display title of the web series", example = "The Expanse",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String title,
        @Schema(description = "Short synopsis", example = "A detective investigates a missing person.")
        String description,
        @Schema(description = "Year the series was first released", example = "2015",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Integer releaseYear) {
}
