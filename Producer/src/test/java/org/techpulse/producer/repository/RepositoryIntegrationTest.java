package org.techpulse.producer.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.techpulse.producer.entity.Episode;
import org.techpulse.producer.entity.Season;
import org.techpulse.producer.entity.WebSeries;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class RepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private WebSeriesRepository webSeriesRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Test
    void persistsSeriesSeasonAndEpisodeRelationships() {
        WebSeries webSeries = new WebSeries();
        webSeries.setTitle("Money Heist");
        webSeries.setDescription("A crime thriller series");
        webSeries.setReleaseYear(2017);
        webSeries = webSeriesRepository.saveAndFlush(webSeries);

        Season season = new Season();
        season.setWebSeries(webSeries);
        season.setSeasonNumber(1);
        season.setTitle("Season 1");
        season = seasonRepository.saveAndFlush(season);

        Episode episode = new Episode();
        episode.setSeason(season);
        episode.setEpisodeNumber(1);
        episode.setTitle("Episode 1");
        episode.setDurationMinutes(45);
        episode = episodeRepository.saveAndFlush(episode);

        assertNotNull(webSeries.getId());
        assertNotNull(season.getId());
        assertNotNull(episode.getId());
        assertEquals(webSeries.getId(), seasonRepository.findById(season.getId()).orElseThrow()
                .getWebSeries().getId());
        assertEquals(season.getId(), episodeRepository.findById(episode.getId()).orElseThrow()
                .getSeason().getId());
    }
}
