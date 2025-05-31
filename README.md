tech requirements:
java version: 17

sudo docker run --name shorten_url_cassandra -d -p 9042:9042 cassandra:4.1

sudo docker run --name shorten-url-redis -d -p 6379:6379 redis:7

sudo docker compose up --build

sudo docker exec -it shorten_url_cassandra cqlsh
sudo docker exec -it shorten-url-redis redis-cli ping
sudo docker exec -it shorten-url-redis redis-cli
