package org.techpulse.consumer.service.impl;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techpulse.consumer.dto.EpisodeResponse;
import org.techpulse.consumer.dto.SeasonResponse;
import org.techpulse.consumer.dto.WebSeriesResponse;
import org.techpulse.consumer.mapper.EntityMapper;
import org.techpulse.consumer.repository.EpisodeRepository;
import org.techpulse.consumer.repository.SeasonRepository;
import org.techpulse.consumer.repository.WebSeriesRepository;
import org.techpulse.consumer.service.CatalogQueryService;

@Service
@Transactional(readOnly = true)
public class CatalogQueryServiceImpl implements CatalogQueryService {

    private final WebSeriesRepository webSeriesRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final EntityMapper mapper;

    public CatalogQueryServiceImpl(WebSeriesRepository webSeriesRepository,
            SeasonRepository seasonRepository, EpisodeRepository episodeRepository, EntityMapper mapper) {
        this.webSeriesRepository = webSeriesRepository;
        this.seasonRepository = seasonRepository;
        this.episodeRepository = episodeRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<WebSeriesResponse> findWebSeries(UUID id) {
        return webSeriesRepository.findById(id).map(mapper::toResponse);
    }

    @Override
    public Optional<SeasonResponse> findSeason(UUID id) {
        return seasonRepository.findById(id).map(mapper::toResponse);
    }

    @Override
    public Optional<EpisodeResponse> findEpisode(UUID id) {
        return episodeRepository.findById(id).map(mapper::toResponse);
    }
}
