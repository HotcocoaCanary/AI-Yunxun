package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.service.Neo4jService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: Neo4jServiceTest
 * @Author: Canary
 * @Date: 2024/8/4 下午7:03
 */
@SpringBootTest
public class Neo4jServiceTest {

    @Resource
    Neo4jService neo4jService;

    @Test
    public void dataInit() {
        neo4jService.deleteAll();
        neo4jService.saveExcelToNeo4j("W:\\Code\\1\\AI-Yunxun\\back-end\\data\\paper.xlsx",
                "W:\\Code\\1\\AI-Yunxun\\back-end\\data\\author.xlsx",
                "W:\\Code\\1\\AI-Yunxun\\back-end\\data\\institution.xlsx",
                "W:\\Code\\1\\AI-Yunxun\\back-end\\data\\country.xlsx");
    }
}