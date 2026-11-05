package org.techpulse.producer.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.techpulse.producer.event.EpisodeEvent;
import org.techpulse.producer.event.SeasonEvent;
import org.techpulse.producer.event.WebSeriesEvent;
import org.techpulse.producer.kafka.KafkaTopics;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            @Value("${app.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.producer.acks}") String acks,
            @Value("${app.kafka.producer.enable-idempotence}") boolean enableIdempotence,
            @Value("${app.kafka.producer.retries}") int retries,
            @Value("${app.kafka.producer.delivery-timeout-ms}") int deliveryTimeoutMs,
            @Value("${app.kafka.producer.request-timeout-ms}") int requestTimeoutMs,
            @Value("${app.kafka.producer.linger-ms}") int lingerMs) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG, acks);
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        properties.put(ProducerConfig.RETRIES_CONFIG, retries);
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        properties.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic webSeriesInfoTopic(
            @Value("${app.kafka.topics.partitions}") int partitions,
            @Value("${app.kafka.topics.replicas}") int replicas) {
        return topic(KafkaTopics.WEBSERIES_INFO, partitions, replicas);
    }

    @Bean
    public NewTopic seasonInfoTopic(
            @Value("${app.kafka.topics.partitions}") int partitions,
            @Value("${app.kafka.topics.replicas}") int replicas) {
        return topic(KafkaTopics.SEASON_INFO, partitions, replicas);
    }

    @Bean
    public NewTopic episodeInfoTopic(
            @Value("${app.kafka.topics.partitions}") int partitions,
            @Value("${app.kafka.topics.replicas}") int replicas) {
        return topic(KafkaTopics.EPISODE_INFO, partitions, replicas);
    }

    private NewTopic topic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
