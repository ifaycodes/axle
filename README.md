# Axle

An event analytics API with built-in rate limiting, caching, and live event streaming — built for developers who want analytics without the third-party dependency.

---

## Features

- **Event ingestion** — record any event (page views, checkouts, clicks) via a single HTTP call
- **Real-time analytics** — query aggregated metrics by URL, event type, date, and hour
- **Live feed** — stream events as they arrive via Server-Sent Events (SSE)
- **Rate limiting** — sliding window log and counter implementations, configurable per IP or API key
- **Caching** — Redis cache-aside pattern serving repeated queries
- **Pagination** — DB-level offset and cursor pagination on raw event queries
- **API key authentication** — registered users can only post and query their own URLs
- **Strategy pattern** — switch between rate limiting implementations via a single config value

---

## Prerequisites

- Java 21
- Docker and Docker Compose
- Maven 3.9+

---

## Running locally

1. Clone the repository:

```bash
git clone https://github.com/yourhandle/axle.git
cd axle
```

2. Create a `.env` file in the project root:

```properties
POSTGRES_DB=axle
POSTGRES_USER=axle
POSTGRES_PASSWORD=axle
REDIS_HOST=redis
REDIS_PORT=6379
RATE_LIMIT_STRATEGY=counter
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_WINDOW=60
RATE_LIMIT_KEY=ip
```

3. Start the services:

```bash
docker-compose up -d
```

Axle is now running at `http://localhost:8080`.

---

## Quickstart

**Generate an API key:**

```bash
curl -X POST http://localhost:8080/keys/generate \
  -H "Content-Type: application/json" \
  -d '{
    "owner": "myapp",
    "urls": ["mysite.com"]
  }'
```

```json
{
  "apiKey": "your-raw-key",
  "owner": "myapp",
  "urls": "[mysite.com]"
}
```

Save the `apiKey` — it is returned once and never stored.

**Record an event:**

```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: your-raw-key" \
  -d '{
    "url": "mysite.com/home",
    "event_type": "page_view"
  }'
```

**Get analytics:**

```bash
curl http://localhost:8080/analytics/mysite.com/home \
  -H "X-API-KEY: your-raw-key"
```

```json
{
  "url": "mysite.com/home",
  "count": 42,
  "date": "2026-08-14"
}
```

---

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `RATE_LIMIT_STRATEGY` | `counter` | Rate limiting implementation: `counter` or `log` |
| `RATE_LIMIT_REQUESTS` | `100` | Maximum requests allowed per window |
| `RATE_LIMIT_WINDOW` | `60` | Window size in seconds |
| `RATE_LIMIT_KEY` | `ip` | Key strategy: `ip`, `apikey`, `ip+endpoint`, `apikey+endpoint` |
| `POSTGRES_DB` | `axle` | PostgreSQL database name |
| `POSTGRES_USER` | `axle` | PostgreSQL username |
| `POSTGRES_PASSWORD` | — | PostgreSQL password |
| `REDIS_HOST` | `redis` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |