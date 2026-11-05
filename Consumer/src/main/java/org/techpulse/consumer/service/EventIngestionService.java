package org.techpulse.consumer.service;

import org.techpulse.consumer.event.EpisodeEvent;
import org.techpulse.consumer.event.SeasonEvent;
import org.techpulse.consumer.event.WebSeriesEvent;

public interface EventIngestionService {
    void process(WebSeriesEvent event);
    void process(SeasonEvent event);
    void process(EpisodeEvent event);
}
