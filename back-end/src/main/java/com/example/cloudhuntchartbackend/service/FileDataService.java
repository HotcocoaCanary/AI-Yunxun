package com.example.cloudhuntchartbackend.service;

import com.example.cloudhuntchartbackend.utils.DataToExcel;
import com.example.cloudhuntchartbackend.utils.FileTool;
import com.example.cloudhuntchartbackend.utils.NbibToData;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Value("${app.upload.dir}")
    private String UPLOAD_DIR;

    @Value("${app.upload.paper}")
    private String paperFilePath;

    @Value("${app.upload.author}")
    private String authorFilePath;

    @Value("${app.upload.institution}")
    private String institutionFilePath;

    @Value("${app.upload.country}")
    private String countryFilePath;

    @Value("${app.upload.replenishment}")
    private String replenishmentFilePath;

    @Value("${app.upload.work.sheet.name}")
    private String SHEET_NAME;

    public String updateFile(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return "没有文件上传。";
        }

        // 确保上传目录存在
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            return "无法创建上传目录: " + UPLOAD_DIR + e.getMessage();
        }

        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null) {
                continue;
            }
            String fileName = file.getOriginalFilename();
            Path destinationFile = uploadPath.resolve(fileName).toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                // 使用Files.move进行原子性的文件替换
                if (Files.exists(destinationFile)) {
                    Files.move(destinationFile, destinationFile.resolveSibling(fileName + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                }
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

                // 如果备份文件存在，删除它
                Path backupFile = destinationFile.resolveSibling(fileName + ".bak");
                if (Files.exists(backupFile)) {
                    Files.delete(backupFile);
                }

            } catch (IOException e) {
                //<TODO description class purpose>
                e.printStackTrace();
            }
        }
        return "所有文件上传成功！";
    }

    public void initData(){
        neo4jService.deleteAll();
        neo4jService.saveExcelToNeo4j(paperFilePath, authorFilePath, institutionFilePath, countryFilePath);
    }

    public void addData(){
        FileTool fileTool = new FileTool();
        try {
            fileTool.excelSplitter(replenishmentFilePath, SHEET_NAME);
            initData();
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
