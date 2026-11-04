package org.techpulse.consumer.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.techpulse.consumer.entity.Season;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
}
