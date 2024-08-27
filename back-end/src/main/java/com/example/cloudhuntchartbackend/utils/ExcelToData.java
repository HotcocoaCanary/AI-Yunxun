package com.example.cloudhuntchartbackend.utils;

import com.example.cloudhuntchartbackend.entity.*;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

@Data
public class ExcelToData {

    private static Map<Integer, Paper> paperMap = new HashMap<>();
    private static Map<String, Author> authorMap = new HashMap<>();
    private static Map<String, Institution> institutionMap = new HashMap<>();
    private static Map<String, Country> countryMap = new HashMap<>();

    public static Map<String, Iterable<?>> loadFiles(String paperPath, String authorPath, String institutionPath, String countryPath) {
        try {
            //必须第一个加载
            loadExcel(paperPath, ExcelToData::loadPaper);
            //必须第二个加载
            loadExcel(authorPath, ExcelToData::loadAuthor);
            //必须第三个加载
            loadExcel(institutionPath, ExcelToData::loadInstitution);
            //必须第四个加载
            loadExcel(countryPath, ExcelToData::loadCountry);
            return getDataMap();
        } catch (IOException e) {
            System.err.println("An error occurred while loading files: " + e.getMessage());
            return null;
        }
    }

    private static void loadExcel(String path, LoadFunction function) throws IOException {
        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headerMap = createHeaderMap(sheet);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                function.loadRow(row, headerMap);
            }
        }
    }

    private static Map<String, Integer> createHeaderMap(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerMap = new HashMap<>();
        if (headerRow != null) {
            DataFormatter formatter = new DataFormatter();
            for (Cell cell : headerRow) {
                headerMap.put(formatter.formatCellValue(cell), cell.getColumnIndex());
            }
        }
        return headerMap;
    }

    private static Map<String, Iterable<?>> getDataMap() {
        Map<String, Iterable<?>> dataMap = new HashMap<>();
        dataMap.put("paper", paperMap.values());
        dataMap.put("author", authorMap.values());
        dataMap.put("institution", institutionMap.values());
        dataMap.put("country", countryMap.values());
        return dataMap;
    }

    // Define functional interface for loading different entity types
    @FunctionalInterface
    interface LoadFunction {
        void loadRow(Row row, Map<String, Integer> headerMap);
    }

    private static void loadPaper(Row row, Map<String, Integer> headerMap) {
        DataFormatter formatter = new DataFormatter();
        Cell pmidCell = row.getCell(headerMap.get("pmid"));
        if (pmidCell != null && !formatter.formatCellValue(pmidCell).isEmpty()) {
            int pmid = (int) pmidCell.getNumericCellValue();
            Paper paper = paperMap.computeIfAbsent(pmid, k -> new Paper());
            for (Cell cell : row) {
                String header = getHeaderForCell(headerMap, cell);
                if (header != null) {
                    String cellValue = formatter.formatCellValue(cell);
                    switch (header) {
                        case "pmid":
                            paper.setPmid(Integer.parseInt(cellValue));
                            break;
                        case "title":
                            paper.setTitle(cellValue);
                            break;
                        case "keyword":
                            paper.setKeyword(cellValue);
                            break;
                        case "date":
                            paper.setDate(cellValue);
                            break;
                        case "journal":
                            paper.setJournal(cellValue);
                            break;
                        case "paper_pmid":
                            Arrays.stream(cellValue.split(","))
                                    .map(String::trim)
                                    .filter(StringUtils::isNotBlank)
                                    .mapToInt(Integer::parseInt)
                                    .forEach(citesPmid -> {
                                        Paper citesPaper = paperMap.get(citesPmid);
                                        if (citesPaper != null) {
                                            paper.getPaperSet().add(citesPaper);
                                        }
                                    });
                            break;
                    }
                }
            }
        }
    }

    private static void loadAuthor(Row row, Map<String, Integer> headerMap) {
        DataFormatter formatter = new DataFormatter();
        Cell authorCell = row.getCell(headerMap.get("name"));
        if (authorCell != null && !formatter.formatCellValue(authorCell).isEmpty()) {
            String name = authorCell.getStringCellValue();
            Author author = authorMap.computeIfAbsent(name, Author::new);
            for (Cell cell : row) {
                String header = getHeaderForCell(headerMap, cell);
                if (header != null) {
                    String cellValue = formatter.formatCellValue(cell);
                    switch (header) {
                        case "name":
                            author.setName(cellValue);
                            break;
                        case "nationality":
                            author.setNationality(cellValue);
                            break;
                        case "paper_pmid":
                            Arrays.stream(cellValue.split(","))
                                    .map(String::trim)
                                    .filter(StringUtils::isNotBlank)
                                    .mapToInt(Integer::parseInt)
                                    .forEach(pmid -> {
                                        Paper paper = paperMap.get(pmid);
                                        if (paper != null) {
                                            author.getPaperSet().add(paper);
                                        }
                                    });
                            break;
                    }
                }
            }
        }
    }

    private static void loadInstitution(Row row, Map<String, Integer> headerMap) {
        DataFormatter formatter = new DataFormatter();
        Cell institutionCell = row.getCell(headerMap.get("name"));
        if (institutionCell != null && !formatter.formatCellValue(institutionCell).isEmpty()) {
            String name = institutionCell.getStringCellValue();
            Institution institution = institutionMap.computeIfAbsent(name, Institution::new);
            for (Cell cell : row) {
                String header = getHeaderForCell(headerMap, cell);
                if (header != null) {
                    String cellValue = formatter.formatCellValue(cell);
                    switch (header) {
                        case "name":
                            institution.setName(cellValue);
                            break;
                        case "author_name":
                            Arrays.stream(cellValue.split(","))
                                    .map(String::trim)
                                    .forEach(authorName -> {
                                        Author author = authorMap.get(authorName);
                                        if (author != null) {
                                            institution.getAuthorSet().add(author);
                                        }
                                    });
                            break;
                    }
                }
            }
        }
    }

    private static void loadCountry(Row row, Map<String, Integer> headerMap) {
        DataFormatter formatter = new DataFormatter();
        Cell countryCell = row.getCell(headerMap.get("nation"));
        if (countryCell != null && !formatter.formatCellValue(countryCell).isEmpty()) {
            String name = countryCell.getStringCellValue();
            Country country = countryMap.computeIfAbsent(name, Country::new);
            for (Cell cell : row) {
                String header = getHeaderForCell(headerMap, cell);
                if (header != null) {
                    String cellValue = formatter.formatCellValue(cell);
                    switch (header) {
                        case "nation":
                            country.setNation(cellValue);
                            break;
                        case "institution_name":
                            Arrays.stream(cellValue.split(","))
                                    .map(String::trim)
                                    .forEach(institutionName -> {
                                        Institution institution = institutionMap.get(institutionName);
                                        if (institution != null) {
                                            country.getInstitutionSet().add(institution);
                                        }
                                    });
                            break;
                    }
                }
            }
        }
    }

    private static String getHeaderForCell(Map<String, Integer> headerMap, Cell cell) {
        return headerMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(cell.getColumnIndex()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

}
