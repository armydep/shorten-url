package com.shortener.repository;

import com.shortener.entity.ShortToLong;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortToLongRepository extends CassandraRepository<ShortToLong, String> {
}