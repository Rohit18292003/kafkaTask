package org.techpulse.producer.mapper;

import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.WebSeriesRequest;
import org.techpulse.producer.dto.WebSeriesResponse;
import org.techpulse.producer.entity.WebSeries;
import org.techpulse.producer.event.WebSeriesEvent;

@Component
public class WebSeriesMapper {

    public WebSeries toEntity(WebSeriesRequest request) {
        WebSeries entity = new WebSeries();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(WebSeries entity, WebSeriesRequest request) {
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setReleaseYear(request.releaseYear());
    }

    public WebSeriesResponse toResponse(WebSeries entity) {
        return new WebSeriesResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getReleaseYear(),
                entity.getCreatedAt());
    }

    public WebSeriesEvent toEvent(WebSeries entity) {
        return new WebSeriesEvent(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getReleaseYear());
    }
}
