package org.techpulse.consumer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.techpulse.consumer.exception.DuplicateEventException;
import org.techpulse.consumer.exception.ParentNotFoundException;

class KafkaConsumerConfigTest {

    @Test
    void sendsNonRetryableDuplicateDirectlyToDeadLetterTopicWithReasonHeader() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        when(template.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
        KafkaConsumerConfig config = config(3);
        DefaultErrorHandler handler = (DefaultErrorHandler) config.kafkaErrorHandler(template);
        ConsumerRecord<String, Object> record = record();

        assertTrue(handler.handleOne(new DuplicateEventException("duplicate id"), record,
                mock(Consumer.class), mock(MessageListenerContainer.class)));

        org.mockito.ArgumentCaptor<ProducerRecord<String, Object>> published =
                org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(template).send(published.capture());
        assertEquals("season-info.DLT", published.getValue().topic());
        assertEquals("duplicate id", new String(
                published.getValue().headers().lastHeader("x-exception-reason").value(),
                StandardCharsets.UTF_8));
    }

    @Test
    void retriesMissingParentThenSendsRecordToDeadLetterTopic() {
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        when(template.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
        DefaultErrorHandler handler = (DefaultErrorHandler) config(2).kafkaErrorHandler(template);
        ConsumerRecord<String, Object> record = record();
        Consumer<?, ?> consumer = mock(Consumer.class);
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        ParentNotFoundException exception = new ParentNotFoundException("Web series", java.util.UUID.randomUUID());

        assertFalse(handler.handleOne(exception, record, consumer, container));
        assertTrue(handler.handleOne(exception, record, consumer, container));

        verify(template).send(any(ProducerRecord.class));
    }

    private KafkaConsumerConfig config(long maxAttempts) {
        return new KafkaConsumerConfig("localhost:9092", "webseries-consumer-group",
                "webseries-info", "season-info", "episode-info", 0, maxAttempts);
    }

    private ConsumerRecord<String, Object> record() {
        return new ConsumerRecord<>("season-info", 0, 7L, "season-id", new Object());
    }
}
