# URL Shortener Service

## 🛠 Tech Requirements
- **Java version**: 17
- **Docker** and **Docker Compose**

---

## 🚀 How to Run the Service

### Option 1: Run Redis and Mongo separately, then start Spring Boot app

1. Start the Mongo container:
   ```bash
   sudo docker run -d -p 27017:27017 --name=mongo -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=password -e MONGO_INITDB_DATABASE=shorten_mongodb mongo:latest
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

- Access Mongo container:
  ```bash
  sudo docker exec -it shorten-url-mongo mongosh -u root -p password
  ```
  
- Access Cassandra container:
  ```bash
  sudo docker exec -it shorten-url-cassandra cqlsh
  ```

- Access Redis container:
  ```bash
  sudo docker exec -it shorten-url-redis redis-cli
  ```

---

## 🧪 Running Tests

### Unit Tests

Run all unit tests with Maven:
```bash
./mvnw test
```

### Performance Test with k6

Run the k6 load test script (e.g., 10 users, 10 iterations):
```bash
k6 run --vus 10 --iterations 10 perf.js
```