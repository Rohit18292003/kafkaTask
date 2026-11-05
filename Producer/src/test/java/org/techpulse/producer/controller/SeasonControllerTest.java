package org.techpulse.producer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.dto.SeasonRequest;
import org.techpulse.producer.dto.SeasonResponse;
import org.techpulse.producer.exception.GlobalExceptionHandler;
import org.techpulse.producer.service.SeasonService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class SeasonControllerTest {

    @Mock
    private SeasonService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new SeasonController(service))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedSeason() throws Exception {
        UUID seriesId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        when(service.create(any(SeasonRequest.class)))
                .thenReturn(new SeasonResponse(seasonId, seriesId, 1, "Season 1", null));

        mockMvc.perform(post("/api/seasons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"webSeriesId":"%s","seasonNumber":1,"title":"Season 1"}
                                """.formatted(seriesId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(seasonId.toString()))
                .andExpect(jsonPath("$.webSeriesId").value(seriesId.toString()));

        verify(service).create(any(SeasonRequest.class));
    }

    @Test
    void createRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/seasons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"webSeriesId":"%s","seasonNumber":0,"title":""}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishReturnsKafkaAcknowledgement() throws Exception {
        UUID seasonId = UUID.randomUUID();
        when(service.publish(seasonId)).thenReturn(new PublishResult("season-info", 2, 17));

        mockMvc.perform(post("/api/seasons/{id}/publish", seasonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("season-info"))
                .andExpect(jsonPath("$.partition").value(2))
                .andExpect(jsonPath("$.offset").value(17));

        verify(service).publish(eq(seasonId));
    }
}
