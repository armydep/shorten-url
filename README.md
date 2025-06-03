# URL Shortener Service

The project implements a scalable, observable, and high-performance URL Shortening Service using Java, Spring Boot, Cassandra, and Redis.

## Tech Stack

- **Language & Framework**: Java 17, Spring Boot 3.4
- **Database**: Apache Cassandra 4.1 (three denormalized tables)
- **Caching**: Redis 7 (hot cache with 1-hour TTL)
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Spring Boot Actuator, Prometheus, Grafana
- **Testing**: JUnit 5
- **Performance Testing**: k6


## Architecture Choices

### Database - Cassandra

Tables:
- `long_to_short`: Maps long URLs to short codes; includes `createdAt`.
- `short_to_long`: Maps short codes to long URLs.
- `click_stats`: Stores redirection count for analytics.

### Caching - Redis

- Caches frequently accessed (hot) short URLs.
- Reduces latency for reads and offloads Cassandra.
- Falls back to Cassandra on cache miss and updates Redis.

### Short Code Generation Method

- Random alphanumeric fixed-length strings.
- DB-based collision detection with retry on conflict.

### Click Counting with Spring Events

- Clicks are tracked asynchronously using Spring’s event publishing mechanism.
- Reduces latency for redirects and decouples counter logic from request handling.

### Monitoring & Metrics

- Prometheus metrics exposed via `/actuator/prometheus`
- Includes: request rates, Redis hit/miss, error tracking.
- Visualized using Grafana dashboards.

### Performance Testing

- Load tested with `k6` (script included).


## How I Handle Scaling and Failover

The service is designed to be horizontally scalable and resilient to failures:

- **Stateless Service Architecture**: The Spring Boot application is stateless, enabling easy horizontal scaling by simply adding more instances.

- **Scalable Storage Components**:
  - **Cassandra**: A distributed NoSQL database built for high availability and horizontal scalability. It handles partitioning and replication automatically, making it suitable for failover and large-scale data operations.
  - **Redis**: An in-memory key-value store optimized for low-latency reads. Redis supports clustering and replication, making it a good fit for caching in scalable, fault-tolerant systems.

### In a Cloud Environment

In a cloud deployment or containerized orchestration environment:

- **Load Balancer**: An external load balancer (e.g., NGINX, AWS ELB) can be used to distribute incoming requests across multiple service nodes to balance traffic.

- **Partitioned Responsibility**: Horizontal partitioning (e.g., consistent hashing based on long URLs or user IDs) can be used to isolate URL shortening responsibilities across nodes, reducing contention and improving throughput.


## Possible Improvements

- **Click Count Buffering**: Click counts are buffered in Redis, flushed to Cassandra in batches.

- **Alternative Key-Value Storage Options**:
  - CouchDB — RESTful, conflict-tolerant, document-based.
  - DynamoDB — Scalable, serverless NoSQL database.
  - LevelDB / RocksDB — High-performance embedded stores.

- **Base62 ID Encoding**: Use globally unique numeric IDs encoded as Base62 for better node isolation and to avoid collisions.

- **URL Sanitization**: Normalize and clean long URLs before storage (e.g., trim, validate protocols, escape characters).

- **Service Decomposition**: Optionally split into two microservices:
  - A **shortening service** that handles URL submissions and writes.
  - A **redirect/stats service** optimized for high-throughput reads.
  - Click tracking can be refactored into an **asynchronous flow**:
    - Emit click events to Kafka.
    - Use a Kafka consumer to batch-update counters in Redis/Cassandra.

- **Advanced Load Testing**: Replace `k6` with [Gatling](https://gatling.io/) for more complex simulation scenarios.