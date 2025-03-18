package com.example.aiyunxun.util;

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExcelUtil {

    public static Map<String, SheetData> readExcel(String filePath) throws IOException {
        Map<String, SheetData> result = new LinkedHashMap<>();
        try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(Paths.get(filePath)))) {
            for (Sheet sheet : workbook) {
                SheetData sheetData = new SheetData();
                List<Map<String, Object>> rows = new ArrayList<>();
                List<String> headers = new ArrayList<>();

                // 读取表头
                Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    for (Cell cell : headerRow) {
                        headers.add(getCellValue(cell).toString());
                    }
                }

                // 读取数据行
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Map<String, Object> rowData = new LinkedHashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        rowData.put(headers.get(j), getCellValue(cell)); // 使用新方法
                    }
                    rows.add(rowData);
                }

                sheetData.setHeaders(headers);
                sheetData.setRows(rows);
                result.put(sheet.getSheetName(), sheetData);
            }
        }
        return result;
    }

    public static void writeExcel(String filePath, Map<String, SheetData> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            for (Map.Entry<String, SheetData> entry : data.entrySet()) {
                String sheetName = sanitizeSheetName(entry.getKey());
                SheetData sheetData = entry.getValue();
                Sheet sheet = workbook.createSheet(sheetName);

                // 创建表头
                Row headerRow = sheet.createRow(0);
                List<String> headers = sheetData.getHeaders();
                for (int i = 0; i < headers.size(); i++) {
                    headerRow.createCell(i).setCellValue(headers.get(i));
                }

                // 填充数据
                List<Map<String, Object>> rows = sheetData.getRows();
                for (int i = 0; i < rows.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    Map<String, Object> rowData = rows.get(i);
                    for (int j = 0; j < headers.size(); j++) {
                        String value = rowData.getOrDefault(headers.get(j), "").toString();
                        row.createCell(j).setCellValue(value);
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }
    }

    public static String sanitizeSheetName(String name) {
        return name.replaceAll("[:\\\\/?*\\[\\]]", "_")
                .substring(0, Math.min(name.length(), 31));
    }

    // ExcelUtil.java 新增类型识别方法
    private static Object getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                }
                yield cell.getNumericCellValue();
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            case STRING -> cell.getStringCellValue().trim();
            default -> "";
        };
    }

    @Data
    public static class SheetData {
        private List<String> headers;
        private List<Map<String, Object>> rows;
    }
}
