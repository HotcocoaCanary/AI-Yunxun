package com.example.aiyunxun.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    //获取当前数据
    void updateDataFromExcel();

    void uploadFiles(List<MultipartFile> files) throws IOException;
}
