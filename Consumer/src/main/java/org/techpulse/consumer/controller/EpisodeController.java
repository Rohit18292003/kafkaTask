package org.techpulse.consumer.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techpulse.consumer.dto.EpisodeResponse;
import org.techpulse.consumer.service.CatalogQueryService;

@RestController
@RequestMapping("/api/episodes")
public class EpisodeController {

    private final CatalogQueryService queryService;

    public EpisodeController(CatalogQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EpisodeResponse> getById(@PathVariable UUID id) {
        return queryService.findEpisode(id).map(ResponseEntity::ok).orElseGet(
                () -> ResponseEntity.notFound().build());
    }
}
