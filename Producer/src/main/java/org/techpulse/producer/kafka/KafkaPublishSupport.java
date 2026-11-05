package org.techpulse.producer.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.exception.KafkaPublishException;

@Component
public class KafkaPublishSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPublishSupport.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final long timeoutSeconds;

    public KafkaPublishSupport(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.publish-timeout-seconds}") long timeoutSeconds) {
        this.kafkaTemplate = kafkaTemplate;
        this.timeoutSeconds = timeoutSeconds;
    }

    public PublishResult publish(String topic, String key, Object event) {
        var future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, error) -> {
            if (error != null) {
                LOGGER.error("Kafka send failed for topic {} and key {}", topic, key, error);
            } else {
                LOGGER.info(
                        "Kafka send acknowledged for topic {}, partition {}, offset {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        try {
            SendResult<String, Object> result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            return new PublishResult(
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Interrupted while waiting for Kafka acknowledgement", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new KafkaPublishException("Kafka did not acknowledge the event", exception);
        }
    }
}
