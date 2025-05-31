package com.shortener.repository;


import com.shortener.entity.ClicksCount;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClicksCountRepository extends CassandraRepository<ClicksCount, String> {
    @Query("UPDATE clicks_count SET count = count + 1 WHERE shorturl = ?0")
    void incrementCounter(String shortUrl);
    @Query("UPDATE clicks_count SET count = count - 1 WHERE shorturl = ?0")
    void decrementCounter(String shortUrl);
}