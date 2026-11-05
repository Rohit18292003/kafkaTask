package org.techpulse.consumer.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.techpulse.consumer.event.WebSeriesEvent;
import org.techpulse.consumer.service.EventIngestionService;

@Component
public class WebSeriesKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(WebSeriesKafkaListener.class);
    private final EventIngestionService ingestionService;

    public WebSeriesKafkaListener(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @KafkaListener(
            topics = "${consumer.kafka.topics.webseries}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "webSeriesKafkaListenerContainerFactory")
    public void consume(WebSeriesEvent event, Acknowledgment acknowledgment,
            ConsumerRecord<String, WebSeriesEvent> record) {
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
