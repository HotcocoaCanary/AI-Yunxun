package com.example.aiyunxun;

import com.example.aiyunxun.repository.Neo4jOperator;
import com.example.aiyunxun.entity.Node;
import com.example.aiyunxun.entity.Relationship;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
public class NBIBDataInitTest {

    @Resource
    private Neo4jOperator neo4jOperator;

    // 使用Map存储节点，键为标签，值为节点集合
    private final Map<String, Set<Node>> nodes = new HashMap<>();
    // 使用Map存储关系，键为关系类型，值为关系集合
    private final Map<String, Set<Relationship>> relationships = new HashMap<>();

    @Test
    public void Neo4jTest() throws IOException {
        String filePath = "C:\\Users\\Canary\\Desktop\\data\\Publications.xlsx";

        try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
            // 处理各个工作表
            // 第一阶段：处理所有节点
            for (Sheet sheet : workbook) {
                processSheetNode(sheet);
            }
// 第二阶段：处理所有关系
            for (Sheet sheet : workbook) {
                processSheetRelationships(sheet);
            }
        }
        for (String label : nodes.keySet()) {
            for (Node node : nodes.get(label)) {
                neo4jOperator.createNode(label, node.getProperties());
            }
        }
        for (String relationType : relationships.keySet()) {
            for (Relationship relationship : relationships.get(relationType)) {
                neo4jOperator.createRelation(relationType, relationship.getStartNode(), relationship.getEndNode(), relationship.getProperties());
            }
        }
    }

    private void processSheetRelationships(Sheet sheet) {
        String sheetName = sheet.getSheetName();
        switch (sheetName) {
            case "Publication":
                processPublicationsSheetRelationships(sheet);
                break;
            case "Institution":
                processInstitutionsSheetRelationships(sheet);
                break;
            case "Author":
                processAuthorsSheetRelationships(sheet);
                break;
            case "Keyword":
                processKeywordsSheetRelationships(sheet);
                break;
            default:
                System.err.println("Unknown sheet: " + sheetName);
        }
    }

    private void processKeywordsSheetNode(Sheet sheet) {
        //表结构：pmid	keyword
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            // 读取并规范化字段（带非空校验）
            String keyword = getNormalizedCellValue(row.getCell(1), true);
            // 关键字段缺失则跳过
            if (keyword == null) {
                continue;
            }
            // 创建Keyword节点
            Map<String, Object> keywordProps = Map.of(
                    "id", keyword,
                    "name", keyword
            );
            Node node = new Node("Keyword", keywordProps);
            nodes.computeIfAbsent("Keyword", k -> new HashSet<>()).add(node);
        }
    }

    private void processAuthorsSheetNode(Sheet sheet) {
        //表结构pmid	author_name	institution	country
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            // 读取并规范化字段（带非空校验）
            String author = getNormalizedCellValue(row.getCell(1), true);
            String country = getNormalizedCellValue(row.getCell(3), false);

            // 关键字段缺失则跳过
            if (author == null) {
                continue;
            }
            String[] name = author.split(",");
            for (String authorName : name) {
                // 创建Institution节点
                Map<String, Object> instProps = Map.of(
                        "id", authorName.trim(),
                        "name", authorName.trim()
                );
                Node node = new Node("Author", instProps);
                nodes.computeIfAbsent("Author", k -> new HashSet<>()).add(node);
            }

            if (country != null) {
                // 获取或者创建Country节点
                Map<String, Object> countryProps = Map.of(
                        "id", country,
                        "name", country
                );
                Node countryNode = new Node("Country", countryProps);
                nodes.computeIfAbsent("Country", k -> new HashSet<>()).add(countryNode);
            }
        }
    }

    private void processInstitutionsSheetNode(Sheet sheet) {
//表结构：institution_name	country
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            // 读取并规范化字段（带非空校验）
            String institution = getNormalizedCellValue(row.getCell(0), true);
            String country = getNormalizedCellValue(row.getCell(1), false);

            // 关键字段缺失则跳过
            if (institution == null) {
                continue;
            }
            // 创建Institution节点
            Map<String, Object> instProps = Map.of(
                    "id", institution,
                    "name", institution
            );
            Node node = new Node("Institution", instProps);
            nodes.computeIfAbsent("Institution", k -> new HashSet<>()).add(node);

            if (country != null) {
                // 获取或者创建Country节点
                Map<String, Object> countryProps = Map.of(
                        "id", country,
                        "name", country
                );
                Node countryNode = new Node("Country", countryProps);
                nodes.computeIfAbsent("Country", k -> new HashSet<>()).add(countryNode);
            }
        }
    }

    private void processPublicationsSheetNode(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            // 读取并规范化字段（带非空校验）
            String pmid = getNormalizedCellValue(row.getCell(0), true); // pmid为必填
            String title = getNormalizedCellValue(row.getCell(1), false);
            String journal = getNormalizedCellValue(row.getCell(2), false);
            String date = getNormalizedCellValue(row.getCell(3), false);

            // 关键字段缺失则跳过
            if (pmid == null) {
                System.err.println("Skipping invalid publication row: " + row.getRowNum());
                continue;
            }

            // 构建属性Map（自动过滤空值）
            Map<String, Object> pubProps = new HashMap<>();
            addValidProperty(pubProps, "id", pmid);
            addValidProperty(pubProps, "name", pmid);
            addValidProperty(pubProps, "title", title);
            addValidProperty(pubProps, "journal", journal);
            addValidProperty(pubProps, "date", date);
            Node node = new Node("Publication", pubProps);
            nodes.computeIfAbsent("Publication", k -> new HashSet<>()).add(node);
        }
    }

    private void processSheetNode(Sheet sheet) {
        String sheetName = sheet.getSheetName();
        switch (sheetName) {
            case "Publication":
                processPublicationsSheetNode(sheet);
                break;
            case "Institution":
                processInstitutionsSheetNode(sheet);
                break;
            case "Author":
                processAuthorsSheetNode(sheet);
                break;
            case "Keyword":
                processKeywordsSheetNode(sheet);
                break;
            default:
                System.err.println("Unknown sheet: " + sheetName);
        }
    }

    private void processKeywordsSheetRelationships(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String pmid = getNormalizedCellValue(row.getCell(0), true); // pmid为必填
            String keyword = getNormalizedCellValue(row.getCell(1), true);
            if (keyword == null || pmid == null) {
                continue;
            }

            // 查询节点
            Node keywordNode = findNodeByProperty("Keyword", keyword);
            if (keywordNode == null) continue;
            Node publicationNode = findNodeByProperty("Publication", pmid);
            if (publicationNode != null) {
                Relationship relationship = new Relationship("Keyword_Publication", keywordNode, publicationNode, new HashMap<>());
                relationships.computeIfAbsent("Keyword_Publication", k -> new HashSet<>()).add(relationship);
            }
        }
    }

    private void processAuthorsSheetRelationships(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String pmid = getNormalizedCellValue(row.getCell(0), true);
            String author = getNormalizedCellValue(row.getCell(1), true);
            String institution = getNormalizedCellValue(row.getCell(2), false);
            String country = getNormalizedCellValue(row.getCell(3), false);

            if (pmid == null || author == null) continue;

            String[] authorNames = author.split(",");
            for (String name : authorNames) {
                name = name.trim();

                // 查找当前Author节点
                Node authorNode = findNodeByProperty("Author", name);
                if (authorNode == null) continue;

                // 处理Author_Publication关系
                Node publicationNode = findNodeByProperty("Publication", pmid);
                if (publicationNode != null) {
                    Relationship rel = new Relationship("AUTHORED", authorNode, publicationNode, new HashMap<>());
                    relationships.computeIfAbsent("AUTHORED", k -> new HashSet<>()).add(rel);
                }

                // 处理Author_Institution关系
                if (institution != null) {
                    Node institutionNode = findNodeByProperty("Institution", institution);
                    if (institutionNode != null) {
                        Relationship rel = new Relationship("AFFILIATED_WITH", authorNode, institutionNode, new HashMap<>());
                        relationships.computeIfAbsent("AFFILIATED_WITH", k -> new HashSet<>()).add(rel);
                    }
                }

                // 处理Author_Country关系
                if (country != null) {
                    Node countryNode = findNodeByProperty("Country", country);
                    if (countryNode != null) {
                        Relationship rel = new Relationship("FROM_COUNTRY", authorNode, countryNode, new HashMap<>());
                        relationships.computeIfAbsent("FROM_COUNTRY", k -> new HashSet<>()).add(rel);
                    }
                }
            }
        }
    }

    private void processInstitutionsSheetRelationships(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String institution = getNormalizedCellValue(row.getCell(0), true);
            String country = getNormalizedCellValue(row.getCell(1), true);
            if (institution == null || country == null) {
                continue;
            }
            Node institutionNode = findNodeByProperty("Institution", institution);
            Node countryNode = findNodeByProperty("Country", country);
            if (institutionNode != null && countryNode != null) {
                Relationship relationship = new Relationship("LOCATED_IN", institutionNode, countryNode, new HashMap<>());
                relationships.computeIfAbsent("LOCATED_IN", k -> new HashSet<>()).add(relationship);
            }
        }
    }

    private void processPublicationsSheetRelationships(Sheet sheet) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String pmid = getNormalizedCellValue(row.getCell(0), true); // pmid为必填
            String institution = getNormalizedCellValue(row.getCell(4), true);
            String country = getNormalizedCellValue(row.getCell(5), true);

            if (institution == null || country == null || pmid == null) {
                continue;
            }
            Node publicationNode = findNodeByProperty("Publication", pmid);
            if (publicationNode != null) {
                Node institutionNode = findNodeByProperty("Institution", institution);
                if (institutionNode != null) {
                    Relationship relationship = new Relationship("PUBLISHED_BY", publicationNode, institutionNode, new HashMap<>());
                    relationships.computeIfAbsent("PUBLISHED_BY", k -> new HashSet<>()).add(relationship);
                }
                Node countryNode = findNodeByProperty("Country", country);
                if (countryNode != null) {
                    Relationship relationship = new Relationship("PUBLISHED_IN", publicationNode, countryNode, new HashMap<>());
                    relationships.computeIfAbsent("PUBLISHED_IN", k -> new HashSet<>()).add(relationship);
                }
            }
        }
    }

    private String getNormalizedCellValue(Cell cell, boolean required) {
        String rawValue = getCellValue(cell); // 原始获取逻辑

        return Optional.ofNullable(rawValue)
                .map(String::trim) // 去除首尾空格
                .filter(v -> !v.isEmpty()) // 过滤空字符串
                .orElse(null);
    }

    // 辅助方法：仅当值有效时才添加到属性
    private void addValidProperty(Map<String, Object> props, String key, String value) {
        if (value != null && !value.isEmpty()) {
            props.put(key, value);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {  // 判断日期类型
                    yield formatDateCell(cell);  // 调用日期格式化方法
                } else {
                    yield formatNumericCell(cell);  // 处理普通数字
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    // 日期格式化（带时分秒）
    private String formatDateCell(Cell cell) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(cell.getDateCellValue());
        } catch (Exception e) {
            return "Invalid Date";
        }
    }

    // 数字格式化（自动处理整数/小数）
    private String formatNumericCell(Cell cell) {
        double value = cell.getNumericCellValue();
        if (value == (int) value) {
            return String.valueOf((int) value);  // 整数去小数点
        } else {
            return String.valueOf(value);  // 保留小数
        }
    }

    private Node findNodeByProperty(String label, String propertyValue) {
        Set<Node> nodeSet = nodes.getOrDefault(label, Collections.emptySet());
        return nodeSet.stream()
                .filter(node -> propertyValue.equals(node.getProperties().get("name")))
                .findFirst()
                .orElse(null);
    }
}