package org.techpulse.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PublishResult(
        @Schema(description = "Kafka topic that received the event", example = "webseries-events")
        String topic,
        @Schema(description = "Kafka partition that received the event", example = "0")
        int partition,
        @Schema(description = "Kafka offset assigned to the event", example = "42")
        long offset) {
}
