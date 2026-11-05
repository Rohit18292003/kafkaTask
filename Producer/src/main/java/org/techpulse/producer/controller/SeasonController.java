package org.techpulse.producer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.techpulse.producer.dto.ApiError;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.dto.SeasonRequest;
import org.techpulse.producer.dto.SeasonResponse;
import org.techpulse.producer.service.SeasonService;

@RestController
@RequestMapping("/api/seasons")
@Tag(name = "Seasons", description = "Create, retrieve, update, and publish seasons")
public class SeasonController {

    private final SeasonService service;

    public SeasonController(SeasonService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a season")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Season created",
                    content = @Content(schema = @Schema(implementation = SeasonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Season number already exists for this web series",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SeasonResponse create(@Valid @RequestBody SeasonRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a season by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Season found",
                    content = @Content(schema = @Schema(implementation = SeasonResponse.class))),
            @ApiResponse(responseCode = "400", description = "The ID is not a valid UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Season not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SeasonResponse get(
            @Parameter(description = "Season UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a season")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Season updated",
                    content = @Content(schema = @Schema(implementation = SeasonResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed or ID is invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Season not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Season number already exists for this web series",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SeasonResponse update(
            @Parameter(description = "Season UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id,
            @Valid @RequestBody SeasonRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a season event to Kafka",
            description = "Waits for the Kafka broker acknowledgement and returns the event location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event published",
                    content = @Content(schema = @Schema(implementation = PublishResult.class))),
            @ApiResponse(responseCode = "400", description = "The ID is not a valid UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Season not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Kafka could not acknowledge the event",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PublishResult publish(
            @Parameter(description = "Season UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id) {
        return service.publish(id);
    }
}
