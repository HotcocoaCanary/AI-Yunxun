package com.example.aiyunxun.controller;

import com.example.aiyunxun.common.Response;
import com.example.aiyunxun.service.FileService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
public class FileController {

    @Resource
    private FileService fileService;

    @Value("${data.path}")
    private String dataPath;

    @GetMapping("/download/{fileName}")
    public ResponseEntity<FileSystemResource> downloadFile(@PathVariable String fileName) {
        // 获取文件路径，这里假设文件存储在项目根目录下的 uploads 文件夹中
        String filePath = System.getProperty(dataPath) + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            // 设置响应头信息，包括文件名和文件类型
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            // 返回文件资源
            return ResponseEntity.ok()
                  .headers(headers)
                  .body(new FileSystemResource(file));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/uploadFile")
    public Response<String> upload(@RequestParam("file") List<MultipartFile> files) {
        try {
            fileService.uploadFiles(files);
        } catch (IOException e) {
            return Response.error("上传失败");
        }
        return Response.success("上传成功");
    }

}
