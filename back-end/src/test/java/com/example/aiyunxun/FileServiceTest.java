package com.example.aiyunxun;

import com.example.aiyunxun.service.FileService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileServiceTest {

    @Resource
    private FileService fileService;

    @Test
    public void testDownloadExcel() {
        fileService.updateDataFromExcel();
    }

    @Test
    public void testUploadExcel() {
        fileService.downloadDataToExcel();
    }
}
