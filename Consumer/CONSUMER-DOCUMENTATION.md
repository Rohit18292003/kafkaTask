# Consumer Service Guide

## What this service does

The Consumer is a standalone Spring Boot service in the `Consumer` directory. It
reads web-series catalog events from Kafka, validates and stores them in
PostgreSQL, and provides read-only HTTP endpoints to look up stored records.
`Producer` is a separate module and is not required to build the Consumer.

The main flow is:

1. A producer publishes a JSON event to a catalog topic.
2. The matching Kafka listener deserializes and validates the event.
3. The Consumer stores a new record or updates an existing record.
4. The listener acknowledges the Kafka offset after successful processing.
5. Failed records are retried according to the retry policy and then sent to
   the corresponding dead-letter topic (DLT).

## Kafka topics

| Purpose | Topic | Event type |
|---|---|---|
| Web-series catalog | `webseries-info` | `WebSeriesEvent` |
| Seasons | `season-info` | `SeasonEvent` |
| Episodes | `episode-info` | `EpisodeEvent` |
| Failed web-series records | `webseries-info.DLT` | Original web-series record |
| Failed season records | `season-info.DLT` | Original season record |
| Failed episode records | `episode-info.DLT` | Original episode record |

The Consumer declares these topics at startup. The current topic configuration
uses one partition and one replica per topic, suitable for local development and
testing rather than a highly available production Kafka cluster.

All three listeners use the consumer group `webseries-consumer-group`. The
bootstrap server defaults to `localhost:9092`; set
`KAFKA_BOOTSTRAP_SERVERS` to use another address. For example, the Docker Compose
Consumer connects to `kafka:29092`.

The consumer starts at the earliest available offset when a group has no saved
offset. Automatic offset commits are disabled. The listener uses manual
acknowledgment so a record is acknowledged only after its database operation
succeeds.

## Event JSON contracts

Events are JSON objects. UUID values must be valid UUID strings; required text
must not be blank, and numeric limits are validated before persistence.

### `webseries-info`

```json
{
  "id": "11111111-1111-4111-8111-111111111111",
  "title": "Example Series",
  "description": "A sample description",
  "releaseYear": 2024
}
```

`id`, `title`, and `releaseYear` are required. `description` may be `null`.
`releaseYear` must be between 1800 and 9999.

### `season-info`

```json
{
  "id": "22222222-2222-4222-8222-222222222222",
  "webSeriesId": "11111111-1111-4111-8111-111111111111",
  "seasonNumber": 1,
  "title": "Season One"
}
```

All fields are required. `webSeriesId` must refer to a web series already
stored by the Consumer, and `seasonNumber` must be positive.

### `episode-info`

```json
{
  "id": "33333333-3333-4333-8333-333333333333",
  "seasonId": "22222222-2222-4222-8222-222222222222",
  "episodeNumber": 1,
  "title": "Pilot",
  "durationMinutes": 45
}
```

All fields are required. `seasonId` must refer to a season already stored by the
Consumer. `episodeNumber` and `durationMinutes` must be positive.

Publish parent records before their children: web series, then seasons, then
episodes. A missing parent causes processing to fail and be retried; if the
parent does not arrive before retries are exhausted, the child record goes to
its DLT.

## Retry and dead-letter handling

The default retry settings are:

| Setting | Default |
|---|---:|
| Retry interval | 1000 ms |
| Maximum deliveries | 3 (initial attempt plus 2 retries) |

After retries are exhausted, the record is published to the original topic name
with `.DLT` appended. The error handler adds an `x-exception-reason` header with
the root exception message when available.

Invalid payloads, duplicate events, and deserialization failures are classified
as non-retryable and are sent to the DLT without repeated processing attempts.
Other processing failures, including a missing parent record, use the retry
policy. A record is not acknowledged as successfully processed until its
database transaction completes.

The retry interval and delivery count can be overridden with
`consumer.kafka.retry.interval-ms` and `consumer.kafka.retry.max-attempts`.

## Persistence and event updates

The Consumer stores three related entity types in PostgreSQL:

| Entity | Table | Relationship / uniqueness |
|---|---|---|
| Web series | `web_series` | Primary key is event `id` |
| Season | `seasons` | References a web series; season number is unique within a web series |
| Episode | `episodes` | References a season; episode number is unique within a season |

The tables are created or updated by Hibernate using `ddl-auto: update`.
`received_at` is set when a record is first stored; `updated_at` is set on
creation and refreshed when a record changes.

By default, an event with a new ID inserts a record. A repeated event whose
content is unchanged is treated as a duplicate. A repeated ID with changed
content updates the existing record. Set
`CONSUMER_ALLOW_UPDATE_ON_MODIFIED_EVENT=false` to reject changed events instead
of updating them. Unique constraint violations are handled as duplicate events.

## HTTP API

The service provides read-only lookup endpoints. `{id}` must be a UUID.

| Method | Path | Successful response |
|---|---|---|
| `GET` | `/api/webseries/{id}` | Web series record |
| `GET` | `/api/seasons/{id}` | Season record, including `webSeriesId` |
| `GET` | `/api/episodes/{id}` | Episode record, including `seasonId` |

Each endpoint returns `404 Not Found` when the ID is not in the database.
Responses include the entity fields plus `receivedAt` and `updatedAt` timestamps.
OpenAPI documentation is available at `/swagger-ui.html`; the schema is at
`/v3/api-docs`.

## Configuration

The following environment variables override the local defaults:

| Environment variable | Default |
|---|---|
| `CONSUMER_DATABASE_URL` | `jdbc:postgresql://localhost:5432/webseries_consumer_db` |
| `CONSUMER_DATABASE_USERNAME` | `postgres` |
| `CONSUMER_DATABASE_PASSWORD` | `tiger` |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `CONSUMER_ALLOW_UPDATE_ON_MODIFIED_EVENT` | `true` |

The database credentials shown are development defaults from the local
configuration. Use appropriately managed credentials outside a local
development environment.

## Run locally

From the `Consumer` directory, start PostgreSQL and Kafka using Docker Compose:

```powershell
docker compose up -d postgres kafka
```

Then run the application:

```powershell
.\mvnw.cmd spring-boot:run
```

The Consumer listens on port `8080`. Open
`http://localhost:8080/swagger-ui.html` to view and try the API.

To run the Consumer and its dependencies together:

```powershell
docker compose up --build
```

To run its tests:

```powershell
.\mvnw.cmd clean test
```

The integration tests use Testcontainers and are skipped when Docker is
unavailable.
