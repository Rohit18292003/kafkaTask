package org.techpulse.consumer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "episodes", uniqueConstraints = @UniqueConstraint(
        name = "uk_episodes_season_episode_number",
        columnNames = {"season_id", "episode_number"}))
public class Episode {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_episodes_season"))
    private Season season;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Episode() {
    }

    public Episode(UUID id, Season season, Integer episodeNumber, String title, Integer durationMinutes) {
        this.id = id;
        this.season = season;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.durationMinutes = durationMinutes;
    }

    public UUID getId() { return id; }
    public Season getSeason() { return season; }
    public Integer getEpisodeNumber() { return episodeNumber; }
    public String getTitle() { return title; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(Season season, Integer episodeNumber, String title, Integer durationMinutes) {
        this.season = season;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.durationMinutes = durationMinutes;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        Instant now = Instant.now();
        receivedAt = now;
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
