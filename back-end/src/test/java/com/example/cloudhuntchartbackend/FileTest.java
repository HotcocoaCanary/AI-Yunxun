package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.utils.FileTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Canary
 * @version 1.0.0
 * @title FileTest
 * @description <TODO description class purpose>
 * @creat 2024/10/9 下午3:46
 **/
public class FileTest {

    @Test
    public void test() throws IOException {
        FileTool ft = new FileTool();
        ft.excelSplitter();
    }
}
