package org.techpulse.consumer;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.PostgreSQLContainer;
import org.techpulse.consumer.dto.EpisodeResponse;
import org.techpulse.consumer.dto.SeasonResponse;
import org.techpulse.consumer.dto.WebSeriesResponse;
import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.event.SeasonEvent;
import org.techpulse.consumer.event.WebSeriesEvent;
import org.techpulse.consumer.repository.EpisodeRepository;
import org.techpulse.consumer.repository.SeasonRepository;
import org.techpulse.consumer.repository.WebSeriesRepository;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConsumerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Value("${local.server.port}")
    private int serverPort;
    @Autowired
    private WebSeriesRepository webSeriesRepository;
    @Autowired
    private SeasonRepository seasonRepository;
    @Autowired
    private EpisodeRepository episodeRepository;

    @BeforeEach
    void clearDatabase() {
        episodeRepository.deleteAllInBatch();
        seasonRepository.deleteAllInBatch();
        webSeriesRepository.deleteAllInBatch();
    }

    @Test
    void consumesOutOfOrderParentThenPersistsFullSeriesAndServesReadEndpoints() throws Exception {
        UUID webSeriesId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();

        publish("season-info", seasonId, new SeasonEvent(seasonId, webSeriesId, 1, "Season 1"));
        publish("webseries-info", webSeriesId,
                new WebSeriesEvent(webSeriesId, "Money Heist", "A crime thriller series", 2017));
        publish("episode-info", episodeId,
                new EpisodeEvent(episodeId, seasonId, 1, "Episode 1", 45));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(
                () -> assertEquals(1, episodeRepository.count()));
        assertEquals(webSeriesId, seasonRepository.findById(seasonId).orElseThrow().getWebSeries().getId());
        assertEquals(seasonId, episodeRepository.findById(episodeId).orElseThrow().getSeason().getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> seriesResponse = get(client, "/api/webseries/" + webSeriesId);
        HttpResponse<String> seasonResponse = get(client, "/api/seasons/" + seasonId);
        HttpResponse<String> episodeResponse = get(client, "/api/episodes/" + episodeId);
        assertEquals(200, seriesResponse.statusCode());
        assertEquals("Money Heist", objectMapper.readValue(seriesResponse.body(), WebSeriesResponse.class).title());
        assertEquals(200, seasonResponse.statusCode());
        assertEquals(webSeriesId,
                objectMapper.readValue(seasonResponse.body(), SeasonResponse.class).webSeriesId());
        assertEquals(200, episodeResponse.statusCode());
        assertEquals(seasonId, objectMapper.readValue(episodeResponse.body(), EpisodeResponse.class).seasonId());
    }

    @Test
    void sendsDuplicateToDltAndAppliesModifiedEvent() throws Exception {
        UUID webSeriesId = UUID.randomUUID();
        WebSeriesEvent initial = new WebSeriesEvent(webSeriesId, "Original", "Description", 2017);
        publish("webseries-info", webSeriesId, initial);
        await().atMost(Duration.ofSeconds(20)).untilAsserted(
                () -> assertEquals("Original", webSeriesRepository.findById(webSeriesId).orElseThrow().getTitle()));

        publish("webseries-info", webSeriesId, initial);
        publish("webseries-info", webSeriesId,
                new WebSeriesEvent(webSeriesId, "Modified", "Description", 2017));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(
                () -> assertEquals("Modified", webSeriesRepository.findById(webSeriesId).orElseThrow().getTitle()));
        assertEquals(1, webSeriesRepository.count());
        assertDuplicateEventWasPublishedToDlt(webSeriesId);
    }

    private void assertDuplicateEventWasPublishedToDlt(UUID eventId) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        JsonDeserializer<WebSeriesEvent> valueDeserializer = new JsonDeserializer<>(WebSeriesEvent.class, false);
        valueDeserializer.addTrustedPackages("org.techpulse.consumer.event");

        boolean found = false;
        try (KafkaConsumer<String, WebSeriesEvent> consumer =
                new KafkaConsumer<>(properties, new StringDeserializer(), valueDeserializer)) {
            consumer.subscribe(List.of("webseries-info.DLT"));
            Instant deadline = Instant.now().plusSeconds(20);
            while (!found && Instant.now().isBefore(deadline)) {
                for (ConsumerRecord<String, WebSeriesEvent> record : consumer.poll(Duration.ofMillis(500))) {
                    if (eventId.toString().equals(record.key())) {
                        Header reason = record.headers().lastHeader("x-exception-reason");
                        assertTrue(reason != null);
                        assertTrue(new String(reason.value()).contains("already exists"));
                        found = true;
                        break;
                    }
                }
            }
        }
        assertTrue(found, "Duplicate event was not published to the DLT");
    }

    private void publish(String topic, UUID key, Object event) throws Exception {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class.getName());
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>(topic, key.toString(), objectMapper.writeValueAsString(event))).get();
        }
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
