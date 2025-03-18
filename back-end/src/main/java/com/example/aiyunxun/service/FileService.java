package com.example.aiyunxun.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    void uploadFiles(List<MultipartFile> files) throws IOException;
    void updateDataFromExcel();
    void downloadDataToExcel();
}
