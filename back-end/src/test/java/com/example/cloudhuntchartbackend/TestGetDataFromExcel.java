package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.example.cloudhuntchartbackend.utils.ExcelToData;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

/**
 * @Description: dd
 * @Author: Canary
 * @Date: 2024/8/3 下午10:53
 */
@SpringBootTest
public class TestGetDataFromExcel {

    @Resource
    private Neo4jService neo4jService;

    @Test
    public void test() {
        Map<String, Iterable<?>> map= ExcelToData.loadFiles("E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\paper.xlsx",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\author.xlsx",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\institution.xlsx",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\country.xlsx");
        if (map != null) {
//            System.out.println(map.get("paper"));
//            System.out.println(map.get("author"));
//            System.out.println(map.get("institution"));
            System.out.println(map.get("country"));

        }
    }
}
