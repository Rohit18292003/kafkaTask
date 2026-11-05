package org.techpulse.producer.service;

import java.util.UUID;
import org.techpulse.producer.dto.EpisodeRequest;
import org.techpulse.producer.dto.EpisodeResponse;
import org.techpulse.producer.dto.PublishResult;

public interface EpisodeService {

    EpisodeResponse create(EpisodeRequest request);

    EpisodeResponse get(UUID id);

    EpisodeResponse update(UUID id, EpisodeRequest request);

    PublishResult publish(UUID id);
}
