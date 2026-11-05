package org.techpulse.producer.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.techpulse.producer.entity.WebSeries;

public interface WebSeriesRepository extends JpaRepository<WebSeries, UUID> {
}
