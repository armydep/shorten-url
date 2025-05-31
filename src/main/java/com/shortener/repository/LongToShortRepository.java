package com.shortener.repository;


import com.shortener.entity.LongToShort;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LongToShortRepository extends CassandraRepository<LongToShort, String> {
}