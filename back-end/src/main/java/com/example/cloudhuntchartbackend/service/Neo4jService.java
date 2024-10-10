package com.example.cloudhuntchartbackend.service;

import com.example.cloudhuntchartbackend.entity.Author;
import com.example.cloudhuntchartbackend.entity.Country;
import com.example.cloudhuntchartbackend.entity.Institution;
import com.example.cloudhuntchartbackend.entity.Paper;
import com.example.cloudhuntchartbackend.repository.AuthorRepository;
import com.example.cloudhuntchartbackend.repository.CountryRepository;
import com.example.cloudhuntchartbackend.repository.InstitutionRepository;
import com.example.cloudhuntchartbackend.repository.PaperRepository;
import com.example.cloudhuntchartbackend.utils.EntityToCypherQuery;
import com.example.cloudhuntchartbackend.utils.ExcelToData;
import com.example.cloudhuntchartbackend.utils.NormalizedData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.MapAccessor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private InstitutionRepository institutionRepository;

    @Resource
    private EntityExtractorService entityExtractorService;

    @Resource
    private Driver driver;

    public void deleteAll() {
        countryRepository.deleteAll();
        institutionRepository.deleteAll();
        authorRepository.deleteAll();
        paperRepository.deleteAll();
    }

    public void addAllPaper(Iterable<Paper> papers) {
        paperRepository.saveAll(papers);
    }

    public void addAllAuthor(Iterable<Author> authors) {
        authorRepository.saveAll(authors);
    }

    public void addAllInstitution(Iterable<Institution> institutions) {
        institutionRepository.saveAll(institutions);
    }

    public void addAllCountry(Iterable<Country> countries) {
        countryRepository.saveAll(countries);
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

    public ObjectNode findAll(int limits) {
        String cypherQuery = "MATCH p=()-[]->() RETURN p LIMIT " + limits;
        List<Map<String, Object>> data = executeCypherQueries(Collections.singletonList(cypherQuery));
        return new NormalizedData().normalizedCypher(data);
    }

    public ObjectNode getAnswer(String answer) {
        Map<String, Object> entity = entityExtractorService.extractEntityAttributes(answer);
        List<String> cypherQuery = new EntityToCypherQuery().getCypherQuery(entity);
        List<Map<String, Object>> data = executeCypherQueries(cypherQuery);
        return new NormalizedData().normalizedCypher(data);
    }

    private List<Map<String, Object>> executeCypherQueries(List<String> cypherQueries) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        try (Session session = driver.session()) {
            for (String cypherQuery : cypherQueries) {
                Result result = session.run(cypherQuery);
                allResults.addAll(result.list(MapAccessor::asMap));
            }
        }
        return allResults;
    }

}
