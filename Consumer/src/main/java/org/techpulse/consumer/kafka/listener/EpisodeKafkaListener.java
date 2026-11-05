package org.techpulse.consumer.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.service.EventIngestionService;

@Component
public class EpisodeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(EpisodeKafkaListener.class);
    private final EventIngestionService ingestionService;

    public EpisodeKafkaListener(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @KafkaListener(
            topics = "${consumer.kafka.topics.episode}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "episodeKafkaListenerContainerFactory")
    public void consume(EpisodeEvent event, Acknowledgment acknowledgment,
            ConsumerRecord<String, EpisodeEvent> record) {
        try {
            ingestionService.process(event);
            acknowledgment.acknowledge();
        } catch (RuntimeException exception) {
            log.error("Kafka event rejected topic={} partition={} offset={} key={} eventId={} reason={}",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    event == null ? null : event.id(), exception.getMessage(), exception);
            throw exception;
        }
    }
}
