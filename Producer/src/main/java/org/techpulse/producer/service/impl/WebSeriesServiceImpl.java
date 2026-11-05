package org.techpulse.producer.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.dto.WebSeriesRequest;
import org.techpulse.producer.dto.WebSeriesResponse;
import org.techpulse.producer.entity.WebSeries;
import org.techpulse.producer.exception.ResourceNotFoundException;
import org.techpulse.producer.kafka.WebSeriesEventPublisher;
import org.techpulse.producer.mapper.WebSeriesMapper;
import org.techpulse.producer.repository.WebSeriesRepository;
import org.techpulse.producer.service.WebSeriesService;

@Service
@Transactional
public class WebSeriesServiceImpl implements WebSeriesService {

    private final WebSeriesRepository repository;
    private final WebSeriesMapper mapper;
    private final WebSeriesEventPublisher publisher;

    public WebSeriesServiceImpl(
            WebSeriesRepository repository,
            WebSeriesMapper mapper,
            WebSeriesEventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Override
    public WebSeriesResponse create(WebSeriesRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public WebSeriesResponse get(UUID id) {
        return mapper.toResponse(findById(id));
    }

    @Override
    public WebSeriesResponse update(UUID id, WebSeriesRequest request) {
        WebSeries entity = findById(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PublishResult publish(UUID id) {
        return publisher.publish(mapper.toEvent(findById(id)));
    }

    private WebSeries findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebSeries not found: " + id));
    }
}
