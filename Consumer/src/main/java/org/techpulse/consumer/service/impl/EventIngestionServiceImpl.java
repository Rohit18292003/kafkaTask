package org.techpulse.consumer.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techpulse.consumer.entity.Episode;
import org.techpulse.consumer.entity.Season;
import org.techpulse.consumer.entity.WebSeries;
import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.event.SeasonEvent;
import org.techpulse.consumer.event.WebSeriesEvent;
import org.techpulse.consumer.exception.DuplicateEventException;
import org.techpulse.consumer.exception.InvalidEventException;
import org.techpulse.consumer.exception.ParentNotFoundException;
import org.techpulse.consumer.exception.UniqueConstraintViolationMapper;
import org.techpulse.consumer.repository.EpisodeRepository;
import org.techpulse.consumer.repository.SeasonRepository;
import org.techpulse.consumer.repository.WebSeriesRepository;
import org.techpulse.consumer.service.EventIngestionService;

@Service
public class EventIngestionServiceImpl implements EventIngestionService {

    private final WebSeriesRepository webSeriesRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final Validator validator;
    private final boolean allowUpdateOnModifiedEvent;

    public EventIngestionServiceImpl(
            WebSeriesRepository webSeriesRepository,
            SeasonRepository seasonRepository,
            EpisodeRepository episodeRepository,
            Validator validator,
            @Value("${consumer.validation.allow-update-on-modified-event:true}")
                    boolean allowUpdateOnModifiedEvent) {
        this.webSeriesRepository = webSeriesRepository;
        this.seasonRepository = seasonRepository;
        this.episodeRepository = episodeRepository;
        this.validator = validator;
        this.allowUpdateOnModifiedEvent = allowUpdateOnModifiedEvent;
    }

    @Override
    @Transactional
    public void process(WebSeriesEvent event) {
        validate(event);
        WebSeries existing = webSeriesRepository.findById(event.id()).orElse(null);
        if (existing == null) {
            save(() -> webSeriesRepository.saveAndFlush(
                    new WebSeries(event.id(), event.title(), event.description(), event.releaseYear())), "web series");
            return;
        }

        boolean identical = existing.getTitle().equals(event.title())
                && java.util.Objects.equals(existing.getDescription(), event.description())
                && existing.getReleaseYear().equals(event.releaseYear());
        if (identical || !allowUpdateOnModifiedEvent) {
            throw duplicate("Web series event already exists: " + event.id());
        }

        existing.update(event.title(), event.description(), event.releaseYear());
        save(() -> webSeriesRepository.saveAndFlush(existing), "web series");
    }

    @Override
    @Transactional
    public void process(SeasonEvent event) {
        validate(event);
        WebSeries parent = webSeriesRepository.findById(event.webSeriesId())
                .orElseThrow(() -> new ParentNotFoundException("Web series", event.webSeriesId()));
        Season existing = seasonRepository.findById(event.id()).orElse(null);
        if (existing == null) {
            save(() -> seasonRepository.saveAndFlush(
                    new Season(event.id(), parent, event.seasonNumber(), event.title())), "season");
            return;
        }

        boolean identical = existing.getWebSeries().getId().equals(event.webSeriesId())
                && existing.getSeasonNumber().equals(event.seasonNumber())
                && existing.getTitle().equals(event.title());
        if (identical || !allowUpdateOnModifiedEvent) {
            throw duplicate("Season event already exists: " + event.id());
        }

        existing.update(parent, event.seasonNumber(), event.title());
        save(() -> seasonRepository.saveAndFlush(existing), "season");
    }

    @Override
    @Transactional
    public void process(EpisodeEvent event) {
        validate(event);
        Season parent = seasonRepository.findById(event.seasonId())
                .orElseThrow(() -> new ParentNotFoundException("Season", event.seasonId()));
        Episode existing = episodeRepository.findById(event.id()).orElse(null);
        if (existing == null) {
            save(() -> episodeRepository.saveAndFlush(new Episode(event.id(), parent, event.episodeNumber(),
                    event.title(), event.durationMinutes())), "episode");
            return;
        }

        boolean identical = existing.getSeason().getId().equals(event.seasonId())
                && existing.getEpisodeNumber().equals(event.episodeNumber())
                && existing.getTitle().equals(event.title())
                && existing.getDurationMinutes().equals(event.durationMinutes());
        if (identical || !allowUpdateOnModifiedEvent) {
            throw duplicate("Episode event already exists: " + event.id());
        }

        existing.update(parent, event.episodeNumber(), event.title(), event.durationMinutes());
        save(() -> episodeRepository.saveAndFlush(existing), "episode");
    }

    private void validate(Object event) {
        if (event == null) {
            throw new InvalidEventException("Event payload must not be null");
        }
        Set<ConstraintViolation<Object>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            String reason = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new InvalidEventException("Event validation failed: " + reason);
        }
    }

    private void save(Runnable operation, String entityName) {
        try {
            operation.run();
        } catch (DataIntegrityViolationException exception) {
            throw UniqueConstraintViolationMapper.toDuplicateIfUniqueViolation(exception, entityName);
        }
    }

    private DuplicateEventException duplicate(String message) {
        return new DuplicateEventException(message);
    }
}
