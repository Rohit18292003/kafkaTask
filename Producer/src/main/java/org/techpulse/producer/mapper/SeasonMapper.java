package org.techpulse.producer.mapper;

import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.SeasonRequest;
import org.techpulse.producer.dto.SeasonResponse;
import org.techpulse.producer.entity.Season;
import org.techpulse.producer.entity.WebSeries;
import org.techpulse.producer.event.SeasonEvent;

@Component
public class SeasonMapper {

    public Season toEntity(SeasonRequest request, WebSeries webSeries) {
        Season entity = new Season();
        updateEntity(entity, request, webSeries);
        return entity;
    }

    public void updateEntity(Season entity, SeasonRequest request, WebSeries webSeries) {
        entity.setWebSeries(webSeries);
        entity.setSeasonNumber(request.seasonNumber());
        entity.setTitle(request.title());
    }

    public SeasonResponse toResponse(Season entity) {
        return new SeasonResponse(
                entity.getId(),
                entity.getWebSeries().getId(),
                entity.getSeasonNumber(),
                entity.getTitle(),
                entity.getCreatedAt());
    }

    public SeasonEvent toEvent(Season entity) {
        return new SeasonEvent(
                entity.getId(),
                entity.getWebSeries().getId(),
                entity.getSeasonNumber(),
                entity.getTitle());
    }
}
