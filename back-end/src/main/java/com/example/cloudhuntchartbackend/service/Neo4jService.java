package com.example.cloudhuntchartbackend.service;

import com.example.cloudhuntchartbackend.entity.Author;
import com.example.cloudhuntchartbackend.entity.Country;
import com.example.cloudhuntchartbackend.entity.Institution;
import com.example.cloudhuntchartbackend.entity.Paper;
import com.example.cloudhuntchartbackend.repository.AuthorRepository;
import com.example.cloudhuntchartbackend.repository.CountryRepository;
import com.example.cloudhuntchartbackend.repository.InstitutionRepository;
import com.example.cloudhuntchartbackend.repository.PaperRepository;
import com.example.cloudhuntchartbackend.utils.ExcelToData;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class Neo4jService {

    @Resource
    private AuthorRepository authorRepository;

    @Resource
    private PaperRepository paperRepository;

    @Resource
    private CountryRepository countryRepository;

    @Resource
    private InstitutionRepository institutionRepository; // 注意首字母小写

    public int addAllPaper(Iterable<Paper> papers) {
        paperRepository.saveAll(papers);
        return 0;
    }

    public int addAllAuthor(Iterable<Author> authors) {
        authorRepository.saveAll(authors);
        return 0;
    }

    public int addAllInstitution(Iterable<Institution> institutions) {
        institutionRepository.saveAll(institutions);
        return 0;
    }

    public int addAllCountry(Iterable<Country> countries) {
        countryRepository.saveAll(countries);
        return 0;
    }

    public int deleteAll() {
        countryRepository.deleteAll();
        institutionRepository.deleteAll();
        authorRepository.deleteAll();
        paperRepository.deleteAll();
        return 0;
    }

    public void saveExcelToNeo4j(String paperPath, String authorPath, String institutionPath, String countryPath) {
        // 使用方法引用和Map的增强for循环来简化代码
        Map<String, Iterable<?>> dataMap = ExcelToData.loadFiles(paperPath, authorPath, institutionPath, countryPath);
        if (dataMap != null) {
            dataMap.forEach((key, value) -> {
                switch (key) {
                    case "paper":
                        addAllPaper((Iterable<Paper>) value);
                        break;
                    case "author":
                        addAllAuthor((Iterable<Author>) value);
                        break;
                    case "institution":
                        addAllInstitution((Iterable<Institution>) value);
                        break;
                    case "country":
                        addAllCountry((Iterable<Country>) value);
                        break;
                    default:
                        // 可以在这里处理未知类型的逻辑或者抛出异常
                        throw new IllegalArgumentException("Unknown data type: " + key);
                }
            });
        }
    }

}
