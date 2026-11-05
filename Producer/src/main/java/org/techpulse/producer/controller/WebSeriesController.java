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
import org.techpulse.producer.dto.WebSeriesRequest;
import org.techpulse.producer.dto.WebSeriesResponse;
import org.techpulse.producer.service.WebSeriesService;

@RestController
@RequestMapping("/api/webseries")
@Tag(name = "Web series", description = "Create, retrieve, update, and publish web series")
public class WebSeriesController {

    private final WebSeriesService service;

    public WebSeriesController(WebSeriesService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a web series")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Web series created",
                    content = @Content(schema = @Schema(implementation = WebSeriesResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "A web series with the same unique key exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public WebSeriesResponse create(@Valid @RequestBody WebSeriesRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a web series by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Web series found",
                    content = @Content(schema = @Schema(implementation = WebSeriesResponse.class))),
            @ApiResponse(responseCode = "400", description = "The ID is not a valid UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Web series not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public WebSeriesResponse get(
            @Parameter(description = "Web series UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a web series")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Web series updated",
                    content = @Content(schema = @Schema(implementation = WebSeriesResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed or ID is invalid",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Web series not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "A web series with the same unique key exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public WebSeriesResponse update(
            @Parameter(description = "Web series UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id,
            @Valid @RequestBody WebSeriesRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a web series event to Kafka",
            description = "Waits for the Kafka broker acknowledgement and returns the event location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event published",
                    content = @Content(schema = @Schema(implementation = PublishResult.class))),
            @ApiResponse(responseCode = "400", description = "The ID is not a valid UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Web series not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "503", description = "Kafka could not acknowledge the event",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PublishResult publish(
            @Parameter(description = "Web series UUID", example = "89b5b26e-4656-4f2c-8a11-e69637cfa312")
            @PathVariable UUID id) {
        return service.publish(id);
    }
}
