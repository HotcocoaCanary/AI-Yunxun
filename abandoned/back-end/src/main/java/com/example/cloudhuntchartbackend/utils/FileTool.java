package com.example.cloudhuntchartbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Canary
 * @version 1.0.0
 * @title FileTool
 * @description 文件操作
 * @creat 2024/10/9 下午2:37
 **/

@Component
public class FileTool {

    @Value("${app.upload.replenishment}")
    private String replenishmentFilePath;

    @Value("${app.upload.work.sheet.name}")
    private String SHEET_NAME;

    private static final String[] OUTPUT_FILE_PATHS = {
            "${app.upload.paper}",
            "${app.upload.author}",
            "${app.upload.institution}",
            "${app.upload.country}"
    };

    private static final String[] HEADERS = {
            "id,pmid,title,date,keyword,journal,CITES,paper_pmid",
            "id,name,AFFILIATED_WITH,author_name",
            "id,name,nationality,AUTHORED,paper_pmid",
            "id,nation,LOCATED_IN,institution_name"
    };

    public void excelSplitter() throws IOException {
        Map<String, Workbook> workbooks = new HashMap<>();
        for (int i = 0; i < OUTPUT_FILE_PATHS.length; i++) {
            String filePath = OUTPUT_FILE_PATHS[i];
            Workbook workbook = createOrLoadWorkbook(filePath);
            getOrCreateSheet(workbook, SHEET_NAME, HEADERS[i].split(","));
            workbooks.put(filePath, workbook);
        }
        readAndSplitData(replenishmentFilePath, workbooks);
        for (Map.Entry<String, Workbook> entry : workbooks.entrySet()) {
            writeWorkbookToFile(entry.getValue(), entry.getKey());
        }
        //<TODO description class purpose>
    }

    private Workbook createOrLoadWorkbook(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                return new SXSSFWorkbook(new XSSFWorkbook(inputStream));
            }
        } else {
            return new SXSSFWorkbook();
        }
    }

    private void getOrCreateSheet(Workbook workbook, String sheetName, String[] headers) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            createHeaderRow(sheet, headers);
        }
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    private void readAndSplitData(String inputFile, Map<String, Workbook> workbooks) throws IOException {
        try (InputStream inputStream = new FileInputStream(inputFile);
             Workbook inputWorkbook = new SXSSFWorkbook(new XSSFWorkbook(inputStream))) {
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
                    Sheet sheet = workbook.getSheet(SHEET_NAME);
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

    public ObjectNode convertExcelToJson(String filePath) throws IOException {
        File excelFile = new File(filePath);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        ArrayNode headersJson = mapper.createArrayNode();
        ArrayNode dataJson = mapper.createArrayNode();

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            Iterator<Row> rowIterator = sheet.iterator();
            // 读取表头
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                Iterator<Cell> cellIterator = headerRow.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    headersJson.add(cell.getStringCellValue());
                }
            }
            // 读取数据
            while (rowIterator.hasNext()) {
                Row dataRow = rowIterator.next();
                Iterator<Cell> cellIterator = dataRow.cellIterator();
                ArrayNode rowDataJson = mapper.createArrayNode();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            rowDataJson.add(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                rowDataJson.add(cell.getDateCellValue().toString());
                            } else {
                                rowDataJson.add(cell.getNumericCellValue());
                            }
                            break;
                        case BOOLEAN:
                            rowDataJson.add(cell.getBooleanCellValue());
                            break;
                        case FORMULA:
                            rowDataJson.add(cell.getCellFormula());
                            break;
                        default:
                            rowDataJson.add("");
                    }
                }
                dataJson.add(rowDataJson);
            }
        }
        // 设置表头和数据到结果JSON对象
        result.set("headers", headersJson);
        result.set("data", dataJson);
        // 返回JSON数据对象
        return result;
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

    private void writeWorkbookToFile(Workbook workbook, String filePath) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        } finally {
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
        }
    }
}

