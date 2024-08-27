package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.entity.Author;
import com.example.cloudhuntchartbackend.entity.Country;
import com.example.cloudhuntchartbackend.entity.Institution;
import com.example.cloudhuntchartbackend.entity.Paper;
import com.example.cloudhuntchartbackend.utils.DataToExcel;
import com.example.cloudhuntchartbackend.utils.NbibToData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

@SpringBootTest
public class TestNbibToExcel {

    @Test
    public void NbibToData() {
        Map<String, Set<?>> map=new NbibToData().loadFiles(
                "E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\AD.nbib",
                "E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\DEP.nbib",
                "E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\FAU.nbib",
                "E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\JT.nbib",
                "E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\MH.nbib",
                "E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\TI.nbib"
        );
        DataToExcel.createPaperExcel((Set<Paper>) map.get("paper"),"E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\paper.xlsx");
        DataToExcel.createAuthorExcel((Set<Author>) map.get("author"),"E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\author.xlsx");
        DataToExcel.createInstitutionExcel((Set<Institution>) map.get("institution"),"E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\institution.xlsx");
        DataToExcel.createCountryExcel((Set<Country>) map.get("country"),"E:\\Code\\Canary\\AI-Cloud-Hunt-Chart\\back-end\\src\\main\\java\\com\\example\\cloudhuntchartbackend\\data\\country.xlsx");

    }
}
