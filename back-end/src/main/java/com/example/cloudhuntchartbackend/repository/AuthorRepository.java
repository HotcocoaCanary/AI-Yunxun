package com.example.cloudhuntchartbackend.repository;

import com.example.cloudhuntchartbackend.entity.Author;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends Neo4jRepository<Author, Long> {
    Author findAuthorById(long id);
}
