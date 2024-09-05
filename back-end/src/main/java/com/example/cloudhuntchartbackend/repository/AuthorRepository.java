package com.example.cloudhuntchartbackend.repository;

import com.example.cloudhuntchartbackend.entity.Author;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 接口
 */
@Repository
public interface AuthorRepository extends Neo4jRepository<Author, Long> {
    List<Author> findByNameContaining(String keyword);

    Author findAuthorById(Long id);

    Author findAuthorByName(String authorName);
}
