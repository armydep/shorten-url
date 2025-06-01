package com.shortener.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;

@Table("clicks_count")
@Data
public class ClicksCount implements Serializable {
    @PrimaryKey
    private String code;
    @CassandraType(type = CassandraType.Name.COUNTER)
    private Long count;
}