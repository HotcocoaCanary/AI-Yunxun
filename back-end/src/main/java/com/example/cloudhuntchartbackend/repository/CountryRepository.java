package com.example.cloudhuntchartbackend.repository;

import com.example.cloudhuntchartbackend.entity.Country;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * 接口
 */
@Repository
public interface CountryRepository extends Neo4jRepository<Country, Long> {
    Country findCountryById(long id);
}
