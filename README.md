# kafkaTask
Pranav and rohit working on this repository

## Build and run

`Producer` and `Consumer` are separate Spring Boot modules. Build or run each from
its own directory; converting `Consumer` to Maven does not combine the modules.

On Windows, run the Consumer tests and application with:

```powershell
cd Consumer
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Use `.\mvnw.cmd package` to create the Consumer JAR under `Consumer\target`.
The existing Producer module can still be built independently with
`cd Producer` followed by `.\mvnw.cmd clean test`.

## Consumer service

The Consumer persists events from `webseries-info`, `season-info`, and
`episode-info` in its own `webseries_consumer_db` database. It exposes
read-only lookup endpoints under `/api/webseries/{id}`, `/api/seasons/{id}`,
and `/api/episodes/{id}`. Swagger UI is available at `/swagger-ui.html`.

Start the Consumer together with its PostgreSQL and Kafka dependencies from the
`Consumer` directory:

```powershell
docker compose up --build
```

For a locally running Consumer, start only the dependencies with
`docker compose up postgres kafka` and then use `.\mvnw.cmd spring-boot:run`.
Database and Kafka addresses can be overridden with `CONSUMER_DATABASE_URL`,
`CONSUMER_DATABASE_USERNAME`, `CONSUMER_DATABASE_PASSWORD`, and
`KAFKA_BOOTSTRAP_SERVERS`. Modified events update existing records by default;
set `CONSUMER_ALLOW_UPDATE_ON_MODIFIED_EVENT=false` to reject all existing IDs.

The Testcontainers integration tests are skipped when Docker is unavailable.
