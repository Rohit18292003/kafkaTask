package org.techpulse.producer.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.dto.SeasonRequest;
import org.techpulse.producer.dto.SeasonResponse;
import org.techpulse.producer.entity.Season;
import org.techpulse.producer.exception.ResourceNotFoundException;
import org.techpulse.producer.kafka.SeasonEventPublisher;
import org.techpulse.producer.mapper.SeasonMapper;
import org.techpulse.producer.repository.SeasonRepository;
import org.techpulse.producer.repository.WebSeriesRepository;
import org.techpulse.producer.service.SeasonService;

@Service
@Transactional
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository repository;
    private final WebSeriesRepository webSeriesRepository;
    private final SeasonMapper mapper;
    private final SeasonEventPublisher publisher;

    public SeasonServiceImpl(
            SeasonRepository repository,
            WebSeriesRepository webSeriesRepository,
            SeasonMapper mapper,
            SeasonEventPublisher publisher) {
        this.repository = repository;
        this.webSeriesRepository = webSeriesRepository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Override
    public SeasonResponse create(SeasonRequest request) {
        Season entity = mapper.toEntity(request, webSeriesRepository.getReferenceById(request.webSeriesId()));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonResponse get(UUID id) {
        return mapper.toResponse(findById(id));
    }

    @Override
    public SeasonResponse update(UUID id, SeasonRequest request) {
        Season entity = findById(id);
        mapper.updateEntity(
                entity,
                request,
                webSeriesRepository.getReferenceById(request.webSeriesId()));
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PublishResult publish(UUID id) {
        return publisher.publish(mapper.toEvent(findById(id)));
    }

    private Season findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Season not found: " + id));
    }
}
