package org.techpulse.producer.service;

import java.util.UUID;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.dto.SeasonRequest;
import org.techpulse.producer.dto.SeasonResponse;

public interface SeasonService {

    SeasonResponse create(SeasonRequest request);

    SeasonResponse get(UUID id);

    SeasonResponse update(UUID id, SeasonRequest request);

    PublishResult publish(UUID id);
}
