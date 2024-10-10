package com.example.cloudhuntchartbackend.service;
import com.example.cloudhuntchartbackend.utils.FileTool;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Canary
 * @version 1.0.0
 * @title sd
 * @description <TODO description class purpose>
 * @creat 2024/10/8 下午5:28
 **/

@Service
public class FileDataService {

    @Resource
    Neo4jService neo4jService;

    FileTool fileTool=new FileTool();
    private static final String UPLOAD_DIR = "data";

    public String updateFile(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return "没有文件上传。";
        }

        // 确保上传目录存在
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            return "无法创建上传目录: "+UPLOAD_DIR+e;
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

            } catch (IOException ignored) {}
        }
        return "所有文件上传成功！";
    }

    public void initData(){
        neo4jService.deleteAll();
        neo4jService.saveExcelToNeo4j("data/paper.xlsx",
                "data/author.xlsx",
                "data/institution.xlsx",
                "data/country.xlsx");
    }

    public void addData(){
        try {
            fileTool.excelSplitter();
            initData();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
