package com.example.aiyunxun.service.impl;

import com.example.aiyunxun.repository.Neo4jOperator;
import com.example.aiyunxun.service.FileService;
import jakarta.annotation.Resource;
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
        // 获取所有节点标签
        List<Map<String, Object>> labels = neo4jOperator.executeCypher("CALL db.labels()");
        List<String> labelsList = labels.stream()
                .map(m -> m.get("label").toString())
                .toList();

        // 获取所有关系类型
        List<Map<String, Object>> relationships = neo4jOperator.executeCypher("""
                MATCH (start)-[rel]->(end)
                WITH DISTINCT type(rel) AS relType, labels(start) AS sLabels, labels(end) AS eLabels
                RETURN relType, sLabels, eLabels""");

        // 创建节点Excel文件
        try (Workbook nodesWorkbook = new XSSFWorkbook()) {
            // 处理每个节点标签
            for (String label : labelsList) {
                String sanitizedLabel = sanitizeSheetName(label);
                Sheet sheet = nodesWorkbook.createSheet(sanitizedLabel);

                // 查询节点数据（包含name属性）
                String cypher = String.format(
                        "MATCH (n:`%s`) RETURN properties(n) AS props, n.name AS name",
                        label.replace("`", "``"));
                List<Map<String, Object>> nodesData = neo4jOperator.executeCypher(cypher);

                // 生成表头
                Set<String> headers = new LinkedHashSet<>();
                headers.add("name");  // 确保name在第一列
                List<Map<String, Object>> rows = new ArrayList<>();

                for (Map<String, Object> row : nodesData) {
                    Map<String, Object> props = (Map<String, Object>) row.get("props");
                    Map<String, Object> dataRow = new LinkedHashMap<>();
                    dataRow.put("name", row.get("name"));
                    dataRow.putAll(props);
                    rows.add(dataRow);
                    headers.addAll(props.keySet());
                }

                // 写入表头
                Row headerRow = sheet.createRow(0);
                int colNum = 0;
                for (String header : headers) {
                    headerRow.createCell(colNum++).setCellValue(header);
                }

                // 写入数据
                int rowNum = 1;
                for (Map<String, Object> data : rows) {
                    Row row = sheet.createRow(rowNum++);
                    colNum = 0;
                    for (String header : headers) {
                        Object value = data.getOrDefault(header, "");
                        row.createCell(colNum++).setCellValue(value.toString());
                    }
                }
            }

            // 保存节点文件
            try (FileOutputStream out = new FileOutputStream(dataPath + "Nodes.xlsx")) {
                nodesWorkbook.write(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 创建关系Excel文件
        try (Workbook relsWorkbook = new XSSFWorkbook()) {
            // 处理每个关系类型
            Set<String> processedRels = new HashSet<>();
            for (Map<String, Object> rel : relationships) {
                String relType = rel.get("relType").toString();
                if (processedRels.contains(relType)) continue;
                processedRels.add(relType);

                String sanitizedRel = sanitizeSheetName(relType);
                Sheet sheet = relsWorkbook.createSheet(sanitizedRel);

                // 查询关系数据（包含节点name）
                String cypher = String.format(
                        "MATCH (start)-[rel:`%s`]->(end) " +
                                "RETURN properties(rel) AS relProps, " +
                                "       start.name AS startName, " +
                                "       end.name AS endName, " +
                                "       labels(start) AS startLabels, " +
                                "       labels(end) AS endLabels",
                        relType.replace("`", "``"));

                List<Map<String, Object>> relsData = neo4jOperator.executeCypher(cypher);

                // 生成表头
                Set<String> headers = new LinkedHashSet<>();
                headers.add("startName");
                headers.add("endName");
                headers.add("startLabels");
                headers.add("endLabels");

                List<Map<String, Object>> rows = new ArrayList<>();
                for (Map<String, Object> row : relsData) {
                    Map<String, Object> dataRow = new LinkedHashMap<>();
                    // 节点信息
                    dataRow.put("startName", row.get("startName"));
                    dataRow.put("endName", row.get("endName"));
                    dataRow.put("startLabels", String.join(",", (List<String>) row.get("startLabels")));
                    dataRow.put("endLabels", String.join(",", (List<String>) row.get("endLabels")));
                    // 关系属性
                    Map<String, Object> relProps = (Map<String, Object>) row.get("relProps");
                    dataRow.putAll(relProps);
                    rows.add(dataRow);
                    headers.addAll(relProps.keySet());
                }

                // 写入表头
                Row headerRow = sheet.createRow(0);
                int colNum = 0;
                for (String header : headers) {
                    headerRow.createCell(colNum++).setCellValue(header);
                }

                // 写入数据
                int rowNum = 1;
                for (Map<String, Object> data : rows) {
                    Row row = sheet.createRow(rowNum++);
                    colNum = 0;
                    for (String header : headers) {
                        Object value = data.getOrDefault(header, "");
                        row.createCell(colNum++).setCellValue(value.toString());
                    }
                }
            }

            // 保存关系文件
            try (FileOutputStream out = new FileOutputStream(dataPath + "Relationships.xlsx")) {
                relsWorkbook.write(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 处理特殊字符（Excel工作表名称规范）
    private String sanitizeSheetName(String name) {
        return name.replaceAll("[:\\\\/?*\\[\\]]", "_")  // 替换非法字符
                .substring(0, Math.min(name.length(), 31));  // 限制长度
    }
}
