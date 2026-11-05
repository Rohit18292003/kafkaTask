package org.techpulse.consumer.config;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.serialization.Deserializer;
import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.event.SeasonEvent;
import org.techpulse.consumer.event.WebSeriesEvent;
import org.techpulse.consumer.exception.DuplicateEventException;
import org.techpulse.consumer.exception.InvalidEventException;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private final String bootstrapServers;
    private final String groupId;
    private final String webSeriesTopic;
    private final String seasonTopic;
    private final String episodeTopic;
    private final long retryIntervalMs;
    private final long maxAttempts;

    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${consumer.kafka.topics.webseries}") String webSeriesTopic,
            @Value("${consumer.kafka.topics.season}") String seasonTopic,
            @Value("${consumer.kafka.topics.episode}") String episodeTopic,
            @Value("${consumer.kafka.retry.interval-ms:1000}") long retryIntervalMs,
            @Value("${consumer.kafka.retry.max-attempts:3}") long maxAttempts) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.webSeriesTopic = webSeriesTopic;
        this.seasonTopic = seasonTopic;
        this.episodeTopic = episodeTopic;
        this.retryIntervalMs = retryIntervalMs;
        this.maxAttempts = maxAttempts;
    }

    @Bean
    public ConsumerFactory<String, WebSeriesEvent> webSeriesConsumerFactory() {
        return consumerFactory(WebSeriesEvent.class);
    }

    @Bean
    public ConsumerFactory<String, SeasonEvent> seasonConsumerFactory() {
        return consumerFactory(SeasonEvent.class);
    }

    @Bean
    public ConsumerFactory<String, EpisodeEvent> episodeConsumerFactory() {
        return consumerFactory(EpisodeEvent.class);
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> eventType) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(eventType, false);
        jsonDeserializer.addTrustedPackages("org.techpulse.consumer.event");
        Deserializer<T> valueDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ProducerFactory<String, Object> deadLetterProducerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, Object> deadLetterKafkaTemplate() {
        return new KafkaTemplate<>(deadLetterProducerFactory());
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> deadLetterKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                deadLetterKafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        recoverer.setHeadersFunction((record, exception) -> {
            org.apache.kafka.common.header.internals.RecordHeaders headers =
                    new org.apache.kafka.common.header.internals.RecordHeaders();
            headers.add("x-exception-reason", exceptionReason(exception).getBytes(StandardCharsets.UTF_8));
            return headers;
        });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer, new FixedBackOff(retryIntervalMs, Math.max(0L, maxAttempts - 1L)));
        errorHandler.addNotRetryableExceptions(
                DuplicateEventException.class, InvalidEventException.class, DeserializationException.class);
        errorHandler.setAckAfterHandle(true);
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) ->
                log.error("Kafka record processing failed topic={} partition={} offset={} key={} "
                                + "deliveryAttempt={} reason={}",
                        record.topic(), record.partition(), record.offset(), record.key(), deliveryAttempt,
                        exceptionReason(exception)));
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WebSeriesEvent> webSeriesKafkaListenerContainerFactory(
            ConsumerFactory<String, WebSeriesEvent> webSeriesConsumerFactory,
            CommonErrorHandler kafkaErrorHandler) {
        return listenerFactory(webSeriesConsumerFactory, kafkaErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SeasonEvent> seasonKafkaListenerContainerFactory(
            ConsumerFactory<String, SeasonEvent> seasonConsumerFactory,
            CommonErrorHandler kafkaErrorHandler) {
        return listenerFactory(seasonConsumerFactory, kafkaErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EpisodeEvent> episodeKafkaListenerContainerFactory(
            ConsumerFactory<String, EpisodeEvent> episodeConsumerFactory,
            CommonErrorHandler kafkaErrorHandler) {
        return listenerFactory(episodeConsumerFactory, kafkaErrorHandler);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(
            ConsumerFactory<String, T> consumerFactory, CommonErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public NewTopic webSeriesTopic() {
        return topic(webSeriesTopic);
    }

    @Bean
    public NewTopic seasonTopic() {
        return topic(seasonTopic);
    }

    @Bean
    public NewTopic episodeTopic() {
        return topic(episodeTopic);
    }

    @Bean
    public NewTopic webSeriesDeadLetterTopic() {
        return topic(webSeriesTopic + ".DLT");
    }

    @Bean
    public NewTopic seasonDeadLetterTopic() {
        return topic(seasonTopic + ".DLT");
    }

    @Bean
    public NewTopic episodeDeadLetterTopic() {
        return topic(episodeTopic + ".DLT");
    }

    private NewTopic topic(String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }

    private static String exceptionReason(Throwable exception) {
        Throwable root = exception;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
