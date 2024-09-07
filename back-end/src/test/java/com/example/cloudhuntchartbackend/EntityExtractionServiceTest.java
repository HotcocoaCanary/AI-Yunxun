package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.repository.AuthorRepository;
import com.example.cloudhuntchartbackend.service.EntityExtractorService;
import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.example.cloudhuntchartbackend.utils.EntityToCypherQuery;
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
    private EntityExtractorService entityExtractor;

    @Resource
    private Neo4jService neo4jService;

    @Resource
    private AuthorRepository authorRepository;

    @Test
    public void test2() throws JsonProcessingException {
        String sentence = "What are the papers written by Varrelmann";
        Map<String, Object> entities = entityExtractor.extractEntityAttributes(sentence);
        List<String> list = new EntityToCypherQuery().getCypherQuery(entities);


        System.out.println(entities);
        System.out.println(list);
    }
}
