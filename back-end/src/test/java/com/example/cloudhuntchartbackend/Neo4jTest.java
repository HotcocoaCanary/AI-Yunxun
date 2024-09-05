package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.repository.AuthorRepository;
import com.example.cloudhuntchartbackend.repository.PaperRepository;
import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.example.cloudhuntchartbackend.service.PaperQuestionService;
import com.example.cloudhuntchartbackend.utils.NormalizedData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class Neo4jTest {

    @Resource
    private Neo4jService neo4jService;

    @Resource
    private PaperQuestionService paperQuestionService;


    @Test
    public void testExtractEntities() throws JsonProcessingException {
        List<Map<String, Object>> maps = neo4jService.executeCypherQuery("MATCH p=()-->() RETURN p LIMIT 25");

        System.out.println(new NormalizedData().processRecords(maps));
    }

    @Test
    public void testNLP() {
        System.out.println(paperQuestionService.answer("What papers has Varrelmann published?"));
    }

}
