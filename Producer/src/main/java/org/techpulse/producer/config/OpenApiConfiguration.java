package org.techpulse.producer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI webSeriesProducerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Web Series Producer API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing web series, seasons, and episodes.
                                Resources are persisted in PostgreSQL. Publish operations send
                                the selected resource as an event to Kafka and return the broker
                                topic, partition, and offset.
                                """));
    }
}
