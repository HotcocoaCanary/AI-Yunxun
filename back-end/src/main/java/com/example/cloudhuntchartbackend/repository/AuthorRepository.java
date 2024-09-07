package com.example.cloudhuntchartbackend.repository;

import com.example.cloudhuntchartbackend.entity.Author;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AuthorRepository extends Neo4jRepository<Author, Long> {

    // 根据作者名查询作者
    Author findAuthorByName(String authorName);

    @Query("MATCH (a)-[r]->(b) RETURN a, r, b")
    List<Map<String, Object>> findAllNodesAndRelationships();
}
