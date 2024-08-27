package com.example.cloudhuntchartbackend.repository;

import com.example.cloudhuntchartbackend.entity.Paper;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * 接口
 */
@Repository
public interface PaperRepository extends Neo4jRepository<Paper, Long> {
}
