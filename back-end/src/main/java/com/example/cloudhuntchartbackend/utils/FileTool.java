package com.example.cloudhuntchartbackend.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Canary
 * @version 1.0.0
 * @title FileTool
 * @description <TODO description class purpose>
 * @creat 2024/10/9 下午2:37
 **/

public class FileTool {

    private static final String[] OUTPUT_FILE_PATHS = {
            "data/paper.xlsx",
            "data/institution.xlsx",
            "data/author.xlsx",
            "data/country.xlsx"
    };
    private static final String[] HEADERS = {
            "id,pmid,title,date,keyword,journal,CITES,paper_pmid",
            "id,name,AFFILIATED_WITH,author_name",
            "id,name,nationality,AUTHORED,paper_pmid",
            "id,nation,LOCATED_IN,institution_name"
    };

    public void excelSplitter() throws IOException {
        String inputFile = "data/replenishment.xlsx"; // 输入Excel文件路径
        Map<String, Workbook> workbooks = new HashMap<>();

        // 初始化输出工作簿
        for (int i = 0; i < OUTPUT_FILE_PATHS.length; i++) {
            String filePath = OUTPUT_FILE_PATHS[i];
            Workbook workbook = getOrCreateWorkbook(filePath);
            String sheetName = "data"; // 假设所有输出文件都有一个名为"data"的工作表
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
                // 创建表头
                String[] headerArray = HEADERS[i].split(",");
                Row headerRow = sheet.createRow(0);
                for (int j = 0; j < headerArray.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headerArray[j]);
                }
            }
            workbooks.put(filePath, workbook);
        }

        // 读取输入文件并分配数据到相应的输出表
        readAndSplitData(inputFile, workbooks);

        // 写入输出文件
        for (Map.Entry<String, Workbook> entry : workbooks.entrySet()) {
            try (OutputStream outputStream = new FileOutputStream(entry.getKey())) {
                entry.getValue().write(outputStream);
            }
        }
    }

    private Workbook getOrCreateWorkbook(String filePath) throws IOException {
        Workbook workbook;
        File file = new File(filePath);
        if (file.exists()) {
            try (FileInputStream inputStream = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(inputStream);
            }
        } else {
            workbook = new XSSFWorkbook();
        }
        return workbook;
    }

    private static void readAndSplitData(String inputFile, Map<String, Workbook> workbooks) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(inputFile);
             Workbook inputWorkbook = new XSSFWorkbook(inputStream)) {
            Sheet inputSheet = inputWorkbook.getSheetAt(0);

            // 读取表头并确定每个表头的列索引
            Row headerRow = inputSheet.getRow(0);
            Map<String, Integer> headerIndexes = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                headerIndexes.put(headerRow.getCell(i).getStringCellValue(), i);
            }

            // 遍历每一行数据
            for (int rowIndex = 1; rowIndex <= inputSheet.getLastRowNum(); rowIndex++) {
                Row row = inputSheet.getRow(rowIndex);

                // 为每个输出表追加数据
                for (int i = 0; i < OUTPUT_FILE_PATHS.length; i++) {
                    Workbook workbook = workbooks.get(OUTPUT_FILE_PATHS[i]);
                    Sheet sheet = workbook.getSheet("data");
                    int newRowNum = sheet.getLastRowNum() + 1; // 获取现有数据的最后一行之后的位置
                    Row newRow = sheet.createRow(newRowNum);

                    // 追加数据到新的行
                    String[] headerArray = HEADERS[i].split(",");
                    for (int j = 0; j < headerArray.length; j++) {
                        String header = headerArray[j];
                        Integer columnIndex = headerIndexes.get(header);
                        if (columnIndex != null) {
                            Cell cell = row.getCell(columnIndex);
                            if (cell != null) {
                                Cell newCell = newRow.createCell(j);
                                copyCell(cell, newCell);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void copyCell(Cell sourceCell, Cell targetCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                targetCell.setCellFormula(sourceCell.getCellFormula());
                break;
            case BLANK:
                targetCell.setCellType(CellType.BLANK);
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            default:
                targetCell.setCellValue("");
        }
    }
}

