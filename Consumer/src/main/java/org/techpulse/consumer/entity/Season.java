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
@Table(name = "seasons", uniqueConstraints = @UniqueConstraint(
        name = "uk_seasons_web_series_season_number",
        columnNames = {"web_series_id", "season_number"}))
public class Season {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "web_series_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_seasons_web_series"))
    private WebSeries webSeries;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Season() {
    }

    public Season(UUID id, WebSeries webSeries, Integer seasonNumber, String title) {
        this.id = id;
        this.webSeries = webSeries;
        this.seasonNumber = seasonNumber;
        this.title = title;
    }

    public UUID getId() { return id; }
    public WebSeries getWebSeries() { return webSeries; }
    public Integer getSeasonNumber() { return seasonNumber; }
    public String getTitle() { return title; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(WebSeries webSeries, Integer seasonNumber, String title) {
        this.webSeries = webSeries;
        this.seasonNumber = seasonNumber;
        this.title = title;
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
