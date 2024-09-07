package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.service.Neo4jService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: dd
 * @Author: Canary
 * @Date: 2024/8/4 下午7:03
 */
@SpringBootTest
public class TestDataToNeo4j {

    @Resource
    Neo4jService neo4jService;

    @Test
    public void test() {
        neo4jService.deleteAll();
        neo4jService.saveExcelToNeo4j("E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\paper.xlsx",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\author.xlsx",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\institution.xlsx",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\country.xlsx");
    }
}