package com.example.aiyunxun.service.impl;

import com.example.aiyunxun.entity.Node;
import com.example.aiyunxun.entity.Relationship;
import com.example.aiyunxun.repository.Neo4jOperator;
import com.example.aiyunxun.service.FileService;
import com.example.aiyunxun.util.ExcelUtil;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private Neo4jOperator neo4jOperator;

    @Value("${data.path}")
    private String dataPath;


    @Override
    public void uploadFiles(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }
        Path uploadPath = Paths.get(dataPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            Path destinationFile = uploadPath.resolve(fileName).toAbsolutePath();
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    @Override
    public void updateDataFromExcel() {
        // 读取节点数据
        Map<String, List<Node>> nodes = readNodeData();
        // 读取关系数据
        Map<String, List<Relationship>> relationships = readRelationshipData(nodes);
        // 批量写入数据库
        neo4jOperator.createNode(nodes);
        neo4jOperator.createRelation(relationships);
    }

    private Map<String, List<Node>> readNodeData() {
        try {
            Map<String, ExcelUtil.SheetData> nodeSheets = ExcelUtil.readExcel(dataPath + "Nodes.xlsx");
            return nodeSheets.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getRows().stream()
                                    .map(row -> {
                                        Node node = new Node();
                                        node.setLabel(e.getKey());
                                        node.setProperties(processNodeProperties(row));
                                        return node;
                                    })
                                    .collect(Collectors.toList())
                    ));
        } catch (IOException e) {
            throw new RuntimeException("节点数据读取失败", e);
        }
    }

    private Map<String, Object> processNodeProperties(Map<String, Object> row) {
        Map<String, Object> properties = new HashMap<>();
        row.forEach((key, value) -> {
            // 保留原始类型
            if (value != null && !value.toString().isEmpty()) {
                properties.put(key, value);
            }
        });
        return properties;
    }

    private Map<String, List<Relationship>> readRelationshipData(Map<String, List<Node>> nodes) {
        try {
            Map<String, ExcelUtil.SheetData> relSheets = ExcelUtil.readExcel(dataPath + "Relationships.xlsx");
            return relSheets.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getRows().stream()
                                    .map(row -> createRelationship(e.getKey(), row, nodes))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
                    ));
        } catch (IOException e) {
            throw new RuntimeException("关系数据读取失败", e);
        }
    }

    private Relationship createRelationship(String relType, Map<String, Object> row, Map<String, List<Node>> nodes) {
        String startLabel = row.get("startLabels").toString();
        String startName = row.get("startName").toString();
        String endLabel = row.get("endLabels").toString();
        String endName = row.get("endName").toString();

        Node start = findNode(nodes, startLabel, startName);
        Node end = findNode(nodes, endLabel, endName);
        if (start == null || end == null) return null;

        Map<String, Object> props = new HashMap<>(row);
        props.remove("startLabels");
        props.remove("startName");
        props.remove("endLabels");
        props.remove("endName");

        return new Relationship(relType, start, end, props);
    }

    private Node findNode(Map<String, List<Node>> nodes, String label, String name) {
        return nodes.getOrDefault(label, Collections.emptyList()).stream()
                .filter(n -> name.equals(n.getProperties().get("name")))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void downloadDataToExcel() {
        exportNodesToExcel();
        exportRelationshipsToExcel();
    }

    private void exportNodesToExcel() {
        Map<String, ExcelUtil.SheetData> nodeData = getLabels().stream()
                .collect(Collectors.toMap(
                        label -> label,
                        this::createNodeSheetData,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        try {
            ExcelUtil.writeExcel(dataPath + "Nodes.xlsx", nodeData);
        } catch (IOException e) {
            throw new RuntimeException("节点数据导出失败", e);
        }
    }

    private ExcelUtil.SheetData createNodeSheetData(String label) {
        List<Map<String, Object>> nodes = neo4jOperator.executeCypher(
                String.format("MATCH (n:`%s`) RETURN properties(n) as props", label)
        );

        Set<String> headers = new LinkedHashSet<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        nodes.forEach(nodeMap -> {
            Map<String, Object> props = (Map<String, Object>) nodeMap.get("props");
            Map<String, Object> row = new LinkedHashMap<>();
            props.forEach((k, v) -> {
                headers.add(k);
                row.put(k, v.toString());
            });
            rows.add(row);
        });

        ExcelUtil.SheetData sheetData = new ExcelUtil.SheetData();
        sheetData.setHeaders(new ArrayList<>(headers));
        sheetData.setRows(rows);
        return sheetData;
    }

    private void exportRelationshipsToExcel() {
        Map<String, ExcelUtil.SheetData> relData = getRelationshipTypes().stream()
                .collect(Collectors.toMap(
                        relType -> relType,
                        this::createRelSheetData,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        try {
            ExcelUtil.writeExcel(dataPath + "Relationships.xlsx", relData);
        } catch (IOException e) {
            throw new RuntimeException("关系数据导出失败", e);
        }
    }

    private ExcelUtil.SheetData createRelSheetData(String relType) {
        String cypher = String.format(
                "MATCH (s)-[r:`%s`]->(e) " +
                        "RETURN properties(r) as props, " +
                        "       s.name as startName, labels(s) as startLabels, " +
                        "       e.name as endName, labels(e) as endLabels",
                relType);

        List<Map<String, Object>> rels = neo4jOperator.executeCypher(cypher);

        Set<String> headers = new LinkedHashSet<>(Arrays.asList("startName", "startLabels", "endName", "endLabels"));
        List<Map<String, Object>> rows = new ArrayList<>();

        rels.forEach(relMap -> {
            Map<String, Object> row = new LinkedHashMap<>();
            // 基础字段
            row.put("startName", relMap.get("startName").toString());
            row.put("endName", relMap.get("endName").toString());
            row.put("startLabels", String.join(",", (List<String>) relMap.get("startLabels")));
            row.put("endLabels", String.join(",", (List<String>) relMap.get("endLabels")));

            // 关系属性
            Map<String, Object> props = (Map<String, Object>) relMap.get("props");
            props.forEach((k, v) -> {
                headers.add(k);
                row.put(k, v.toString());
            });

            rows.add(row);
        });

        ExcelUtil.SheetData sheetData = new ExcelUtil.SheetData();
        sheetData.setHeaders(new ArrayList<>(headers));
        sheetData.setRows(rows);
        return sheetData;
    }

    private List<String> getLabels() {
        return neo4jOperator.executeCypher("CALL db.labels()").stream()
                .map(m -> m.get("label").toString())
                .collect(Collectors.toList());
    }

    private List<String> getRelationshipTypes() {
        return neo4jOperator.executeCypher("CALL db.relationshipTypes()").stream()
                .map(m -> m.get("relationshipType").toString())
                .collect(Collectors.toList());
    }
}
