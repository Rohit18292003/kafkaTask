package org.techpulse.producer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.event.EpisodeEvent;
import org.techpulse.producer.event.SeasonEvent;
import org.techpulse.producer.event.WebSeriesEvent;
import org.techpulse.producer.kafka.EpisodeEventPublisher;
import org.techpulse.producer.kafka.SeasonEventPublisher;
import org.techpulse.producer.kafka.WebSeriesEventPublisher;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:producer-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.admin.auto-create=false"
})
class ProducerApiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaAdmin kafkaAdmin;

    @MockBean
    private WebSeriesEventPublisher webSeriesPublisher;

    @MockBean
    private SeasonEventPublisher seasonPublisher;

    @MockBean
    private EpisodeEventPublisher episodePublisher;

    @Test
    void createsReadsUpdatesAndPublishesAllResourceTypes() throws Exception {
        when(webSeriesPublisher.publish(any()))
                .thenReturn(new PublishResult("webseries-info", 0, 1));
        when(seasonPublisher.publish(any()))
                .thenReturn(new PublishResult("season-info", 1, 2));
        when(episodePublisher.publish(any()))
                .thenReturn(new PublishResult("episode-info", 2, 3));

        JsonNode createdSeries = postJson(
                "/api/webseries",
                """
                        {"title":"Money Heist","description":"A crime thriller series","releaseYear":2017}
                        """);
        String seriesId = createdSeries.get("id").asText();
        mockMvc.perform(get("/api/webseries/{id}", seriesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Money Heist"));

        mockMvc.perform(put("/api/webseries/{id}", seriesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Money Heist Updated","description":"Updated description","releaseYear":2017}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Money Heist Updated"));
        assertPublished("/api/webseries/" + seriesId + "/publish", "webseries-info");
        assertPublished("/api/webseries/" + seriesId + "/publish", "webseries-info");
        verify(webSeriesPublisher, times(2)).publish(new WebSeriesEvent(
                java.util.UUID.fromString(seriesId),
                "Money Heist Updated",
                "Updated description",
                2017));

        JsonNode createdSeason = postJson(
                "/api/seasons",
                """
                        {"webSeriesId":"%s","seasonNumber":1,"title":"Season 1"}
                        """.formatted(seriesId));
        String seasonId = createdSeason.get("id").asText();
        mockMvc.perform(get("/api/seasons/{id}", seasonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.webSeriesId").value(seriesId));

        mockMvc.perform(put("/api/seasons/{id}", seasonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"webSeriesId":"%s","seasonNumber":1,"title":"Season One"}
                                """.formatted(seriesId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Season One"));
        assertPublished("/api/seasons/" + seasonId + "/publish", "season-info");
        verify(seasonPublisher).publish(new SeasonEvent(
                java.util.UUID.fromString(seasonId),
                java.util.UUID.fromString(seriesId),
                1,
                "Season One"));

        JsonNode createdEpisode = postJson(
                "/api/episodes",
                """
                        {"seasonId":"%s","episodeNumber":1,"title":"Episode 1","durationMinutes":45}
                        """.formatted(seasonId));
        String episodeId = createdEpisode.get("id").asText();
        mockMvc.perform(get("/api/episodes/{id}", episodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seasonId").value(seasonId));

        mockMvc.perform(put("/api/episodes/{id}", episodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"seasonId":"%s","episodeNumber":1,"title":"Episode One","durationMinutes":50}
                                """.formatted(seasonId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(50));
        assertPublished("/api/episodes/" + episodeId + "/publish", "episode-info");
        verify(episodePublisher).publish(new EpisodeEvent(
                java.util.UUID.fromString(episodeId),
                java.util.UUID.fromString(seasonId),
                1,
                "Episode One",
                50));
    }

    private JsonNode postJson(String path, String json) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void assertPublished(String path, String topic) throws Exception {
        mockMvc.perform(post(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value(topic));
    }
}
