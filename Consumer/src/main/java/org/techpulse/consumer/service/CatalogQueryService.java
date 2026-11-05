package org.techpulse.consumer.service;

import java.util.Optional;
import java.util.UUID;
import org.techpulse.consumer.dto.EpisodeResponse;
import org.techpulse.consumer.dto.SeasonResponse;
import org.techpulse.consumer.dto.WebSeriesResponse;

public interface CatalogQueryService {
    Optional<WebSeriesResponse> findWebSeries(UUID id);
    Optional<SeasonResponse> findSeason(UUID id);
    Optional<EpisodeResponse> findEpisode(UUID id);
}
