package org.techpulse.producer.kafka;

import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.event.EpisodeEvent;

@Component
public class EpisodeEventPublisher {

    private final KafkaPublishSupport publishSupport;

    public EpisodeEventPublisher(KafkaPublishSupport publishSupport) {
        this.publishSupport = publishSupport;
    }

    public PublishResult publish(EpisodeEvent event) {
        return publishSupport.publish(KafkaTopics.EPISODE_INFO, event.id().toString(), event);
    }
}
