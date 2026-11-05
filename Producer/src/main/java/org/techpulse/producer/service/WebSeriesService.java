package org.techpulse.producer.service;

import java.util.UUID;
import org.techpulse.producer.dto.PublishResult;
import org.techpulse.producer.dto.WebSeriesRequest;
import org.techpulse.producer.dto.WebSeriesResponse;

public interface WebSeriesService {

    WebSeriesResponse create(WebSeriesRequest request);

    WebSeriesResponse get(UUID id);

    WebSeriesResponse update(UUID id, WebSeriesRequest request);

    PublishResult publish(UUID id);
}
