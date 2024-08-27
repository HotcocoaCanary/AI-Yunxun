package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.repository.AuthorRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Neo4jTest {

    @Resource
    private AuthorRepository authorRepository;

    @Test
    public void testExtractEntities() {
        System.out.println(authorRepository.findRelatedNodesAndRelationships());
    }
}
