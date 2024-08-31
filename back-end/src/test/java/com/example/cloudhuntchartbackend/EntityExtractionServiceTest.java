package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.service.EntityExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class EntityExtractionServiceTest {

    @Autowired
    private EntityExtractionService entityExtractionService;

    @Test
    public void testExtractEntities() throws IOException {
        String sentence = "Which writers in China have published papers";
        List<String> entities = entityExtractionService.extractEntities(sentence);
        entities.forEach(System.out::println);
    }
}
