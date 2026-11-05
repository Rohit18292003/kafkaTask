package org.techpulse.producer.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.entity.Season;
import org.techpulse.producer.entity.WebSeries;
import org.techpulse.producer.event.SeasonEvent;
import org.techpulse.producer.exception.KafkaPublishException;
import org.techpulse.producer.exception.ResourceNotFoundException;
import org.techpulse.producer.kafka.SeasonEventPublisher;
import org.techpulse.producer.mapper.SeasonMapper;
import org.techpulse.producer.repository.SeasonRepository;
import org.techpulse.producer.repository.WebSeriesRepository;

@ExtendWith(MockitoExtension.class)
class SeasonServiceImplTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private WebSeriesRepository webSeriesRepository;

    @Mock
    private SeasonEventPublisher publisher;

    private SeasonServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeasonServiceImpl(
                seasonRepository,
                webSeriesRepository,
                new SeasonMapper(),
                publisher);
    }

    @Test
    void publishLoadsSeasonAndPublishesWithoutCheckingParentRepository() {
        UUID seasonId = UUID.randomUUID();
        UUID seriesId = UUID.randomUUID();
        Season season = new Season();
        season.setId(seasonId);
        season.setWebSeries(webSeries(seriesId));
        season.setSeasonNumber(1);
        season.setTitle("Season 1");
        PublishResult expected = new PublishResult("season-info", 1, 42L);
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(publisher.publish(new SeasonEvent(seasonId, seriesId, 1, "Season 1")))
                .thenReturn(expected);

        PublishResult actual = service.publish(seasonId);

        assertEquals(expected, actual);
        verify(seasonRepository).findById(seasonId);
        verify(webSeriesRepository, never()).findById(org.mockito.ArgumentMatchers.any());
        verify(webSeriesRepository, never()).getReferenceById(org.mockito.ArgumentMatchers.any());
        verify(publisher).publish(new SeasonEvent(seasonId, seriesId, 1, "Season 1"));
    }

    @Test
    void publishFailsWhenSeasonDoesNotExist() {
        UUID seasonId = UUID.randomUUID();
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.publish(seasonId));

        verify(webSeriesRepository, never()).findById(org.mockito.ArgumentMatchers.any());
        verify(publisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void publishPropagatesKafkaFailure() {
        UUID seasonId = UUID.randomUUID();
        Season season = new Season();
        season.setId(seasonId);
        season.setWebSeries(webSeries(UUID.randomUUID()));
        season.setSeasonNumber(1);
        season.setTitle("Season 1");
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
        when(publisher.publish(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new KafkaPublishException("Kafka failed", new RuntimeException()));

        assertThrows(KafkaPublishException.class, () -> service.publish(seasonId));
    }

    private WebSeries webSeries(UUID id) {
        WebSeries webSeries = new WebSeries();
        webSeries.setId(id);
        webSeries.setTitle("Series");
        webSeries.setReleaseYear(2020);
        return webSeries;
    }
}
