package org.techpulse.producer.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.techpulse.producer.entity.Episode;

public interface EpisodeRepository extends JpaRepository<Episode, UUID> {
}
