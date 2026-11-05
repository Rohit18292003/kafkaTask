package org.techpulse.consumer.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.techpulse.consumer.entity.Season;
import org.techpulse.consumer.entity.WebSeries;
import org.techpulse.consumer.entity.Episode;
import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.event.SeasonEvent;
import org.techpulse.consumer.event.WebSeriesEvent;
import org.techpulse.consumer.exception.DuplicateEventException;
import org.techpulse.consumer.exception.InvalidEventException;
import org.techpulse.consumer.exception.ParentNotFoundException;
import org.techpulse.consumer.repository.EpisodeRepository;
import org.techpulse.consumer.repository.SeasonRepository;
import org.techpulse.consumer.repository.WebSeriesRepository;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceImplTest {

    @Mock
    private WebSeriesRepository webSeriesRepository;
    @Mock
    private SeasonRepository seasonRepository;
    @Mock
    private EpisodeRepository episodeRepository;
    private Validator validator;

    private EventIngestionServiceImpl service;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new EventIngestionServiceImpl(
                webSeriesRepository, seasonRepository, episodeRepository, validator, true);
    }

    @Test
    void savesNewWebSeries() {
        UUID id = UUID.randomUUID();
        when(webSeriesRepository.findById(id)).thenReturn(Optional.empty());

        service.process(new WebSeriesEvent(id, "Money Heist", "A crime thriller series", 2017));

        verify(webSeriesRepository).saveAndFlush(any(WebSeries.class));
    }

    @Test
    void rejectsIdenticalWebSeriesEvent() {
        UUID id = UUID.randomUUID();
        WebSeries existing = new WebSeries(id, "Money Heist", "A crime thriller series", 2017);
        when(webSeriesRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateEventException.class,
                () -> service.process(new WebSeriesEvent(id, "Money Heist", "A crime thriller series", 2017)));
    }

    @Test
    void updatesModifiedWebSeriesWhenEnabled() {
        UUID id = UUID.randomUUID();
        WebSeries existing = new WebSeries(id, "Old title", "Description", 2017);
        when(webSeriesRepository.findById(id)).thenReturn(Optional.of(existing));

        service.process(new WebSeriesEvent(id, "New title", "Description", 2017));

        assertEquals("New title", existing.getTitle());
        verify(webSeriesRepository).saveAndFlush(existing);
    }

    @Test
    void rejectsModifiedWebSeriesWhenUpdatesAreDisabled() {
        UUID id = UUID.randomUUID();
        WebSeries existing = new WebSeries(id, "Old title", "Description", 2017);
        when(webSeriesRepository.findById(id)).thenReturn(Optional.of(existing));
        service = new EventIngestionServiceImpl(
                webSeriesRepository, seasonRepository, episodeRepository, validator, false);

        assertThrows(DuplicateEventException.class,
                () -> service.process(new WebSeriesEvent(id, "New title", "Description", 2017)));
        assertEquals("Old title", existing.getTitle());
    }

    @Test
    void rejectsSeasonWhenWebSeriesIsMissing() {
        UUID parentId = UUID.randomUUID();
        when(webSeriesRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(ParentNotFoundException.class, () -> service.process(
                new SeasonEvent(UUID.randomUUID(), parentId, 1, "Season 1")));
    }

    @Test
    void savesNewSeasonWhenParentExists() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        when(webSeriesRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.empty());

        service.process(new SeasonEvent(seasonId, parentId, 1, "Season 1"));

        verify(seasonRepository).saveAndFlush(any(Season.class));
    }

    @Test
    void rejectsIdenticalSeasonEvent() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        Season existing = new Season(seasonId, parent, 1, "Season 1");
        when(webSeriesRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateEventException.class,
                () -> service.process(new SeasonEvent(seasonId, parentId, 1, "Season 1")));
    }

    @Test
    void updatesModifiedSeasonWhenEnabled() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        Season existing = new Season(seasonId, parent, 1, "Old title");
        when(webSeriesRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(existing));

        service.process(new SeasonEvent(seasonId, parentId, 1, "New title"));

        assertEquals("New title", existing.getTitle());
        verify(seasonRepository).saveAndFlush(existing);
    }

    @Test
    void mapsSeasonNumberUniqueConstraintToDuplicateEvent() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        when(webSeriesRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.empty());
        when(seasonRepository.saveAndFlush(any(Season.class))).thenThrow(
                new DataIntegrityViolationException("duplicate", new SQLException("unique violation", "23505")));

        assertThrows(DuplicateEventException.class,
                () -> service.process(new SeasonEvent(seasonId, parentId, 1, "Season 1")));
    }

    @Test
    void rejectsEpisodeWhenSeasonIsMissing() {
        UUID seasonId = UUID.randomUUID();
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.empty());

        assertThrows(ParentNotFoundException.class, () -> service.process(
                new EpisodeEvent(UUID.randomUUID(), seasonId, 1, "Episode 1", 45)));
    }

    @Test
    void savesNewEpisodeWhenSeasonExists() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        Season season = new Season(seasonId, parent, 1, "Season 1");
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.empty());

        service.process(new EpisodeEvent(episodeId, seasonId, 1, "Episode 1", 45));

        verify(episodeRepository).saveAndFlush(any(Episode.class));
    }

    @Test
    void rejectsIdenticalEpisodeEvent() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        Season season = new Season(seasonId, parent, 1, "Season 1");
        Episode existing = new Episode(episodeId, season, 1, "Episode 1", 45);
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateEventException.class,
                () -> service.process(new EpisodeEvent(episodeId, seasonId, 1, "Episode 1", 45)));
    }

    @Test
    void updatesModifiedEpisodeWhenEnabled() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        Season season = new Season(seasonId, parent, 1, "Season 1");
        Episode existing = new Episode(episodeId, season, 1, "Old title", 45);
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(existing));

        service.process(new EpisodeEvent(episodeId, seasonId, 1, "New title", 50));

        assertEquals("New title", existing.getTitle());
        assertEquals(50, existing.getDurationMinutes());
        verify(episodeRepository).saveAndFlush(existing);
    }

    @Test
    void mapsEpisodeNumberUniqueConstraintToDuplicateEvent() {
        UUID parentId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        WebSeries parent = new WebSeries(parentId, "Money Heist", null, 2017);
        Season season = new Season(seasonId, parent, 1, "Season 1");
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.empty());
        when(episodeRepository.saveAndFlush(any(Episode.class))).thenThrow(
                new DataIntegrityViolationException("duplicate",
                        new SQLException("unique violation", "23505")));

        assertThrows(DuplicateEventException.class,
                () -> service.process(new EpisodeEvent(episodeId, seasonId, 1, "Episode 1", 45)));
    }

    @Test
    void rejectsNullEventAsInvalid() {
        assertThrows(InvalidEventException.class, () -> service.process((WebSeriesEvent) null));
    }

    @Test
    void rejectsBeanValidationFailure() {
        assertThrows(InvalidEventException.class,
                () -> service.process(new WebSeriesEvent(UUID.randomUUID(), " ", null, 2017)));
    }
}
