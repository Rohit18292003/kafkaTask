package org.techpulse.producer.kafka;

import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.event.WebSeriesEvent;

@Component
public class WebSeriesEventPublisher {

    private final KafkaPublishSupport publishSupport;

    public WebSeriesEventPublisher(KafkaPublishSupport publishSupport) {
        this.publishSupport = publishSupport;
    }

    public PublishResult publish(WebSeriesEvent event) {
        return publishSupport.publish(KafkaTopics.WEBSERIES_INFO, event.id().toString(), event);
    }
}
