package org.techpulse.producer.mapper;

import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.EpisodeRequest;
import org.techpulse.producer.dto.EpisodeResponse;
import org.techpulse.producer.entity.Episode;
import org.techpulse.producer.entity.Season;
import org.techpulse.producer.event.EpisodeEvent;

@Component
public class EpisodeMapper {

    public Episode toEntity(EpisodeRequest request, Season season) {
        Episode entity = new Episode();
        updateEntity(entity, request, season);
        return entity;
    }

    public void updateEntity(Episode entity, EpisodeRequest request, Season season) {
        entity.setSeason(season);
        entity.setEpisodeNumber(request.episodeNumber());
        entity.setTitle(request.title());
        entity.setDurationMinutes(request.durationMinutes());
    }

    public EpisodeResponse toResponse(Episode entity) {
        return new EpisodeResponse(
                entity.getId(),
                entity.getSeason().getId(),
                entity.getEpisodeNumber(),
                entity.getTitle(),
                entity.getDurationMinutes(),
                entity.getCreatedAt());
    }

    public EpisodeEvent toEvent(Episode entity) {
        return new EpisodeEvent(
                entity.getId(),
                entity.getSeason().getId(),
                entity.getEpisodeNumber(),
                entity.getTitle(),
                entity.getDurationMinutes());
    }
}
