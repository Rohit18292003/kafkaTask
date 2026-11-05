package org.techpulse.consumer.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techpulse.consumer.dto.SeasonResponse;
import org.techpulse.consumer.service.CatalogQueryService;

@RestController
@RequestMapping("/api/seasons")
public class SeasonController {

    private final CatalogQueryService queryService;

    public SeasonController(CatalogQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeasonResponse> getById(@PathVariable UUID id) {
        return queryService.findSeason(id).map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.notFound().build());
    }
}
