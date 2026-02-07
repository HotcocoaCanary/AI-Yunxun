package com.example.cloudhuntchartbackend.controller;

import com.example.cloudhuntchartbackend.member.Result;
import com.example.cloudhuntchartbackend.service.FileDataService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Canary
 * @version 1.0.0
 * @title DataController
 * @description <TODO description class purpose>
 * @creat 2024/10/8 下午5:07
 **/
@RestController
public class DataController {

    @Resource
    private FileDataService fileDataService;

    @Value("${app.upload.template}")
    private String templatePath;


    @PostMapping("/data/upload")
    public Result<String> upload(@RequestParam("file") List<MultipartFile> files) {
        String massage = fileDataService.uploadFiles(files);
        return Result.success(massage);
    }

    @PostMapping("/data/update")
    public Result<ObjectNode> update() {
        fileDataService.commitUploads();
        fileDataService.update();
        return Result.success();
    }

    @PostMapping("/data/revert")
    public Result<ObjectNode> revert(@RequestBody Map<String, String> requestBody){
        try {
            String fileName = requestBody.get("fileName");
            fileDataService.revertUpload(fileName);
            return Result.success();
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/data/revertAll")
    public Result<ObjectNode> revertAll(){
        try {
            fileDataService.revertAllUploads();
            return Result.success();
        } catch (IOException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/excel")
    public Result<ObjectNode> excel(@RequestBody Map<String, String> requestBody) {
        String fileName = requestBody.get("fileName");
        int page = Integer.parseInt(requestBody.get("page"));
        int pageSize = Integer.parseInt(requestBody.get("pageSize"));
        ObjectNode data = fileDataService.getExcel(fileName, page, pageSize);
        return Result.success(data);
    }

    @PostMapping("/data/template")
    public Result<String> getTemplate(){
        return Result.success(templatePath);
    }

}
