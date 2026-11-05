package org.techpulse.producer.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techpulse.producer.dto.EpisodeRequest;
import org.techpulse.producer.dto.EpisodeResponse;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.entity.Episode;
import org.techpulse.producer.exception.ResourceNotFoundException;
import org.techpulse.producer.kafka.EpisodeEventPublisher;
import org.techpulse.producer.mapper.EpisodeMapper;
import org.techpulse.producer.repository.EpisodeRepository;
import org.techpulse.producer.repository.SeasonRepository;
import org.techpulse.producer.service.EpisodeService;

@Service
@Transactional
public class EpisodeServiceImpl implements EpisodeService {

    private final EpisodeRepository repository;
    private final SeasonRepository seasonRepository;
    private final EpisodeMapper mapper;
    private final EpisodeEventPublisher publisher;

    public EpisodeServiceImpl(
            EpisodeRepository repository,
            SeasonRepository seasonRepository,
            EpisodeMapper mapper,
            EpisodeEventPublisher publisher) {
        this.repository = repository;
        this.seasonRepository = seasonRepository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Override
    public EpisodeResponse create(EpisodeRequest request) {
        Episode entity = mapper.toEntity(request, seasonRepository.getReferenceById(request.seasonId()));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeResponse get(UUID id) {
        return mapper.toResponse(findById(id));
    }

    @Override
    public EpisodeResponse update(UUID id, EpisodeRequest request) {
        Episode entity = findById(id);
        mapper.updateEntity(
                entity,
                request,
                seasonRepository.getReferenceById(request.seasonId()));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PublishResult publish(UUID id) {
        return publisher.publish(mapper.toEvent(findById(id)));
    }

    private Episode findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found: " + id));
    }
}
