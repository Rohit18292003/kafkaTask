package org.techpulse.consumer.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.techpulse.consumer.entity.WebSeries;

public interface WebSeriesRepository extends JpaRepository<WebSeries, UUID> {
}
