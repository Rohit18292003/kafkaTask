package org.techpulse.producer.kafka;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.techpulse.producer.exception.KafkaPublishException;

@ExtendWith(MockitoExtension.class)
class KafkaPublishSupportTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void failedKafkaSendIsReportedAsPublishFailure() {
        String topic = KafkaTopics.SEASON_INFO;
        String key = UUID.randomUUID().toString();
        Object event = new Object();
        when(kafkaTemplate.send(topic, key, event))
                .thenReturn(CompletableFuture.failedFuture(new KafkaException("broker unavailable")));
        KafkaPublishSupport support = new KafkaPublishSupport(kafkaTemplate, 1);

        assertThrows(KafkaPublishException.class, () -> support.publish(topic, key, event));

        verify(kafkaTemplate).send(topic, key, event);
    }

    @Test
    void publishWaitsForKafkaAcknowledgement() {
        String topic = KafkaTopics.SEASON_INFO;
        String key = UUID.randomUUID().toString();
        Object event = new Object();
        SendResult<String, Object> sendResult = org.mockito.Mockito.mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata metadata =
                org.mockito.Mockito.mock(org.apache.kafka.clients.producer.RecordMetadata.class);
        when(metadata.topic()).thenReturn(topic);
        when(metadata.partition()).thenReturn(2);
        when(metadata.offset()).thenReturn(18L);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(kafkaTemplate.send(topic, key, event))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
        KafkaPublishSupport support = new KafkaPublishSupport(kafkaTemplate, 1);

        var result = support.publish(topic, key, event);

        org.junit.jupiter.api.Assertions.assertEquals(topic, result.topic());
        org.junit.jupiter.api.Assertions.assertEquals(2, result.partition());
        org.junit.jupiter.api.Assertions.assertEquals(18L, result.offset());
    }
}
