package com.example.cloudhuntchartbackend.service;

import com.example.cloudhuntchartbackend.utils.DataToExcel;
import com.example.cloudhuntchartbackend.utils.FileTool;
import com.example.cloudhuntchartbackend.utils.NbibToData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
        import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Canary
 * @version 1.0.0
 * @title sd
 * @description 文件处理
 * @creat 2024/10/8 下午5:28
 **/

@Service
public class FileDataService {

    @Resource
    Neo4jService neo4jService;

    @Resource
    FileTool fileTool;

    @Value("${app.upload.dir}")
    private String UPLOAD_DIR;

    @Value("${app.upload.temp.dir}")
    private String TEMP_UPLOAD_DIR;

    @Value("${app.upload.paper}")
    private String paperFilePath;

    @Value("${app.upload.author}")
    private String authorFilePath;

    @Value("${app.upload.institution}")
    private String institutionFilePath;

    @Value("${app.upload.country}")
    private String countryFilePath;

    public String uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return "没有文件上传。";
        }
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path tempUploadPath = Paths.get(TEMP_UPLOAD_DIR).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            Files.createDirectories(tempUploadPath);
        } catch (IOException e) {
            return "无法创建上传目录: " + UPLOAD_DIR + " 或 " + TEMP_UPLOAD_DIR + e.getMessage();
        }
        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            Path destinationFile = tempUploadPath.resolve(fileName).toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return "文件上传失败: " + fileName;
            }
        }
        return "所有文件已上传到临时目录！";
    }

    public void commitUploads(){
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path tempUploadPath = Paths.get(TEMP_UPLOAD_DIR).toAbsolutePath().normalize();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempUploadPath)) {
            for (Path filePath : directoryStream) {
                Path destinationFile = uploadPath.resolve(filePath.getFileName());
                Files.move(filePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void revertUpload(String fileName) throws IOException {
        Path tempUploadPath = Paths.get(TEMP_UPLOAD_DIR).toAbsolutePath().normalize();
        Path fileToRevert = tempUploadPath.resolve(fileName);
        System.out.println(fileToRevert.toString());
        if (Files.exists(fileToRevert)) {
            Files.delete(fileToRevert);
        }
    }

    public void revertAllUploads() throws IOException {
        Path tempUploadPath = Paths.get(TEMP_UPLOAD_DIR).toAbsolutePath().normalize();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tempUploadPath)) {
            for (Path filePath : directoryStream) {
                System.out.println(filePath);
                Files.delete(filePath);
            }
        }
    }

    public ObjectNode getExcel(String fileName, int page, int pageSize) {
        try {
            String filePath = switch (fileName) {
                case "paper" -> paperFilePath;
                case "author" -> authorFilePath;
                case "institution" -> institutionFilePath;
                case "country" -> countryFilePath;
                default -> fileName;
            };
            // 将Excel文件转换为JsonNode
            ObjectNode excelData = fileTool.convertExcelToJson(filePath);
            JsonNode headers = excelData.get("headers");
            JsonNode data = excelData.get("data");
            // 检查数据是否为ArrayNode
            if (!(data instanceof ArrayNode dataArray)) {
                throw new IllegalArgumentException("Data is not an array");
            }
            int totalRows = dataArray.size();
            // 计算分页的起始索引和结束索引
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalRows);
            // 创建新的ArrayNode以存储分页数据
            ArrayNode paginatedData = dataArray.arrayNode();
            // 手动添加分页数据
            for (int i = startIndex; i < endIndex; i++) {
                paginatedData.add(dataArray.get(i));
            }
            // 构建新的ObjectNode以包含分页数据和总记录数
            ObjectNode paginatedExcelData = dataArray.objectNode();
            paginatedExcelData.set("headers", headers);
            paginatedExcelData.set("data", paginatedData);
            paginatedExcelData.put("total", totalRows);
            return paginatedExcelData;
        } catch (IOException e) {
            // 处理异常，可能需要返回错误信息或null
            e.printStackTrace();
            return null;
        }
    }

    public void update(){
        try {
            fileTool.excelSplitter();
            neo4jService.deleteAll();
            neo4jService.saveExcelToNeo4j(paperFilePath, authorFilePath, institutionFilePath, countryFilePath);
        } catch (IOException e) {
            //<TODO description class purpose>
            System.out.println(e.getMessage());
        }
    }

    public void nbibToExcel(String adPath, String depPath, String fauPath, String jtPath, String mhPath, String tiPath) {
        Map<String, Set<?>> map = new NbibToData().loadFiles(adPath, depPath, fauPath, jtPath, mhPath, tiPath);
        new DataToExcel().getExcel(map, paperFilePath, authorFilePath, institutionFilePath, countryFilePath);
    }
}
