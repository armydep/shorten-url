tech requirements:
java version: 17

1.
sudo docker run --name shorten-url-cassandra -d -p 9042:9042 cassandra:4.1
sudo docker run --name shorten-url-redis -d -p 6379:6379 redis:7
./mvnw spring-boot:run

2.
sudo docker compose up --build

sudo docker exec -it shorten-url-cassandra cqlsh
sudo docker exec -it shorten-url-redis redis-cli ping
sudo docker exec -it shorten-url-redis redis-cli
