package com.example.cloudhuntchartbackend.controller;

import com.example.cloudhuntchartbackend.member.Result;
import com.example.cloudhuntchartbackend.service.FileDataService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping("/data/upload")
    public Result<String> handleFileUpload(@RequestParam("file") List<MultipartFile> files) {
        String massage = fileDataService.updateFile(files);
        return Result.success(massage);
    }

    @PostMapping("/data/init")
    public Result<ObjectNode> init() {
        fileDataService.initData();
        return Result.success();
    }

    @PostMapping("/data/add")
    public Result<ObjectNode> add() {
        fileDataService.addData();
        return Result.success();
    }

}
