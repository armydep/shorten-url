# URL Shortener Service

## 🛠 Tech Requirements
- **Java version**: 17
- **Docker** and **Docker Compose**

---

## 🚀 How to Run the Service

### Option 1: Run Redis and Cassandra separately, then start Spring Boot app

1. Start the Cassandra container (and wait about 1 minute for it to finish initializing):
   ```bash
   sudo docker run --name shorten-url-cassandra -d -p 9042:9042 cassandra:4.1
   ```

2. Start the Redis container:
   ```bash
   sudo docker run --name shorten-url-redis -d -p 6379:6379 redis:7
   ```

3. Start the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

---

### Option 2: Run Everything with Docker Compose

```bash
sudo docker compose up --build
```

This will spin up all services: Redis, Cassandra, and the Spring Boot application.

---

## 📊 Monitoring & Observability

- **Prometheus Metrics**:  
  [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

- **Health Check**:  
  [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 📘 OpenAPI Documentation

- [Swagger UI Docs](http://localhost:8080/docs)

---

## 🐚 Useful Commands

- Access Cassandra container:
  ```bash
  sudo docker exec -it shorten-url-cassandra cqlsh
  ```

- Access Redis container:
  ```bash
  sudo docker exec -it shorten-url-redis redis-cli
  ```