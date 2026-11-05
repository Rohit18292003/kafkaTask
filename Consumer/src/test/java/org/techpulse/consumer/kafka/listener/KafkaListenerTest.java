package org.techpulse.consumer.kafka.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.event.SeasonEvent;
import org.techpulse.consumer.event.WebSeriesEvent;
import org.techpulse.consumer.service.EventIngestionService;

class KafkaListenerTest {

    @Test
    void acknowledgesWebSeriesOnlyAfterServiceSucceeds() {
        EventIngestionService service = mock(EventIngestionService.class);
        WebSeriesKafkaListener listener = new WebSeriesKafkaListener(service);
        WebSeriesEvent event = new WebSeriesEvent(java.util.UUID.randomUUID(), "Money Heist", null, 2017);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        ConsumerRecord<String, WebSeriesEvent> record = new ConsumerRecord<>(
                "webseries-info", 0, 0L, event.id().toString(), event);

        listener.consume(event, acknowledgment, record);

        var order = inOrder(service, acknowledgment);
        order.verify(service).process(event);
        order.verify(acknowledgment).acknowledge();
    }

    @Test
    void doesNotAcknowledgeRejectedWebSeriesEvent() {
        EventIngestionService service = mock(EventIngestionService.class);
        WebSeriesKafkaListener listener = new WebSeriesKafkaListener(service);
        WebSeriesEvent event = new WebSeriesEvent(java.util.UUID.randomUUID(), "Money Heist", null, 2017);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        ConsumerRecord<String, WebSeriesEvent> record = new ConsumerRecord<>(
                "webseries-info", 0, 0L, event.id().toString(), event);
        IllegalStateException failure = new IllegalStateException("database failure");
        whenProcessFails(service, event, failure);

        assertThrows(IllegalStateException.class, () -> listener.consume(event, acknowledgment, record));

        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void acknowledgesSeasonAndEpisodeAfterProcessing() {
        EventIngestionService service = mock(EventIngestionService.class);
        Acknowledgment seasonAck = mock(Acknowledgment.class);
        SeasonEvent season = new SeasonEvent(
                java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 1, "Season 1");
        new SeasonKafkaListener(service).consume(season, seasonAck,
                new ConsumerRecord<>("season-info", 0, 0L, season.id().toString(), season));
        var seasonOrder = inOrder(service, seasonAck);
        seasonOrder.verify(service).process(season);
        seasonOrder.verify(seasonAck).acknowledge();

        Acknowledgment episodeAck = mock(Acknowledgment.class);
        EpisodeEvent episode = new EpisodeEvent(
                java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 1, "Episode 1", 45);
        new EpisodeKafkaListener(service).consume(episode, episodeAck,
                new ConsumerRecord<>("episode-info", 0, 0L, episode.id().toString(), episode));
        var episodeOrder = inOrder(service, episodeAck);
        episodeOrder.verify(service).process(episode);
        episodeOrder.verify(episodeAck).acknowledge();
    }

    private void whenProcessFails(EventIngestionService service, WebSeriesEvent event, RuntimeException failure) {
        org.mockito.Mockito.doThrow(failure).when(service).process(event);
    }
}
