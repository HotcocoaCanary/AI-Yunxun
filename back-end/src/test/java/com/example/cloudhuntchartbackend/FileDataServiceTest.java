package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.service.FileDataService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileDataServiceTest {

    @Resource
    FileDataService fileDataService;

    @Test
    public void NbibToData() {
        fileDataService.nbibToExcel("E:\\Code\\Canary\\AI-Yunxun\\back-end\\data\\AD.nbib",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\data\\DEP.nbib",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\data\\FAU.nbib",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\data\\JT.nbib",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\data\\MH.nbib",
                "E:\\Code\\Canary\\AI-Yunxun\\back-end\\data\\TI.nbib");
    }
}
