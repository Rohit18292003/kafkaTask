package org.techpulse.consumer.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techpulse.consumer.dto.WebSeriesResponse;
import org.techpulse.consumer.service.CatalogQueryService;

@RestController
@RequestMapping("/api/webseries")
public class WebSeriesController {

    private final CatalogQueryService queryService;

    public WebSeriesController(CatalogQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebSeriesResponse> getById(@PathVariable UUID id) {
        return queryService.findWebSeries(id).map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.notFound().build());
    }
}
