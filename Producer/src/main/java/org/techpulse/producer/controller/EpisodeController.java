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
import org.techpulse.producer.dto.EpisodeRequest;
import org.techpulse.producer.dto.EpisodeResponse;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.service.EpisodeService;

@RestController
@RequestMapping("/api/episodes")
@Tag(name = "Episodes", description = "Create, retrieve, update, and publish episodes")
public class EpisodeController {

    private final EpisodeService service;

    public EpisodeController(EpisodeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an episode")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Episode created",
                    content = @Content(schema = @Schema(implementation = EpisodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Episode number already exists for this season",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EpisodeResponse create(@Valid @RequestBody EpisodeRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an episode by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Episode found",
                    content = @Content(schema = @Schema(implementation = EpisodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "The ID is not a valid UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Episode not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EpisodeResponse get(
            @Parameter(description = "Episode UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an episode")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Episode updated",
                    content = @Content(schema = @Schema(implementation = EpisodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed or ID is invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Episode not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Episode number already exists for this season",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EpisodeResponse update(
            @Parameter(description = "Episode UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id,
            @Valid @RequestBody EpisodeRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish an episode event to Kafka",
            description = "Waits for the Kafka broker acknowledgement and returns the event location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event published",
                    content = @Content(schema = @Schema(implementation = PublishResult.class))),
            @ApiResponse(responseCode = "400", description = "The ID is not a valid UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Episode not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Kafka could not acknowledge the event",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PublishResult publish(
            @Parameter(description = "Episode UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id) {
        return service.publish(id);
    }
}
