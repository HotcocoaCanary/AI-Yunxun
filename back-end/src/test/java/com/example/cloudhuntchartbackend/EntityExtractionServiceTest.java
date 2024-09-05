package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.repository.AuthorRepository;
import com.example.cloudhuntchartbackend.service.EntityExtractor;
import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.example.cloudhuntchartbackend.utils.NormalizedData;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class EntityExtractionServiceTest {

    @Autowired
    private EntityExtractor entityExtractor;

    @Resource
    private Neo4jService neo4jService;

    @Resource
    private AuthorRepository authorRepository;

    @Test
    public void test2() throws JsonProcessingException {
        String sentence = "What are the papers written by Varrelmann";
        Map<String, Object> entities = entityExtractor.extractEntityAttributes(sentence);

        System.out.println(entities);
        System.out.println(authorRepository.findAuthorByName(entities.get("Author").toString()));

        String cypher = "MATCH (n) WHERE n.name='"+entities.get("Author").toString()+"' RETURN n";

        List<Map<String, Object>> maps = neo4jService.executeCypherQuery(cypher);
        System.out.println(new NormalizedData().processRecords(maps));
    }
}
