package org.techpulse.producer.kafka;

import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.event.SeasonEvent;

@Component
public class SeasonEventPublisher {

    private final KafkaPublishSupport publishSupport;

    public SeasonEventPublisher(KafkaPublishSupport publishSupport) {
        this.publishSupport = publishSupport;
    }

    public PublishResult publish(SeasonEvent event) {
        return publishSupport.publish(KafkaTopics.SEASON_INFO, event.id().toString(), event);
    }
}
