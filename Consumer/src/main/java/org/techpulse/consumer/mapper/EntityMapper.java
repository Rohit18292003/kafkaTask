package org.techpulse.consumer.mapper;

import org.springframework.stereotype.Component;
import org.techpulse.consumer.dto.EpisodeResponse;
import org.techpulse.consumer.dto.SeasonResponse;
import org.techpulse.consumer.dto.WebSeriesResponse;
import org.techpulse.consumer.entity.Episode;
import org.techpulse.consumer.entity.Season;
import org.techpulse.consumer.entity.WebSeries;

@Component
public class EntityMapper {

    public WebSeriesResponse toResponse(WebSeries entity) {
        return new WebSeriesResponse(entity.getId(), entity.getTitle(), entity.getDescription(),
                entity.getReleaseYear(), entity.getReceivedAt(), entity.getUpdatedAt());
    }

    public SeasonResponse toResponse(Season entity) {
        return new SeasonResponse(entity.getId(), entity.getWebSeries().getId(), entity.getSeasonNumber(),
                entity.getTitle(), entity.getReceivedAt(), entity.getUpdatedAt());
    }

    public EpisodeResponse toResponse(Episode entity) {
        return new EpisodeResponse(entity.getId(), entity.getSeason().getId(), entity.getEpisodeNumber(),
                entity.getTitle(), entity.getDurationMinutes(), entity.getReceivedAt(), entity.getUpdatedAt());
    }
}
