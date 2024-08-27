package com.example.cloudhuntchartbackend.utils;

import com.example.cloudhuntchartbackend.entity.Author;
import com.example.cloudhuntchartbackend.entity.Country;
import com.example.cloudhuntchartbackend.entity.Institution;
import com.example.cloudhuntchartbackend.entity.Paper;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DataToExcel {

    private static final String[] PAPER_HEADERS = {"id", "pmid", "title", "date", "keyword", "journal", "CITES", "paper_pmid"};
    private static final String[] AUTHOR_HEADERS = {"id", "name", "nationality","AUTHORED", "paper_pmid"};
    private static final String[] INSTITUTION_HEADERS = {"id", "name", "AFFILIATED_WITH", "author_name"};
    private static final String[] COUNTRY_HEADERS = {"id", "nation", "LOCATED_IN", "institution_name"};


    public static void createPaperExcel(Set<Paper> setData, String fileName) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(fileName)) {

            SXSSFSheet sheet = workbook.createSheet("Data");

            sheet.trackAllColumnsForAutoSizing();

            // 创建表头
            createHeaders(sheet, PAPER_HEADERS);

            // 填充数据
            int rowNum = 1;
            for (Paper data : setData) {
                String citedPmid = data.getPaperSet().stream()
                        .map(Paper::getPmid) // 返回 Integer
                        .map(String::valueOf) // 将 Integer 转换为 String
                        .collect(Collectors.joining(","));
                createRowForPaper(sheet, data, rowNum++, citedPmid);
            }


            // 自动调整所有列的宽度
            for (int i = 0; i < PAPER_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createAuthorExcel(Set<Author> setData, String fileName) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(fileName)) {

            SXSSFSheet sheet = workbook.createSheet("Data");


            sheet.trackAllColumnsForAutoSizing();

            // 创建表头
            createHeaders(sheet, AUTHOR_HEADERS);

            // 填充数据
            int rowNum = 1;
            for (Author data : setData) {
                String paperPmid = data.getPaperSet().stream()
                        .map(Paper::getPmid) // 返回 Integer
                        .map(String::valueOf) // 将 Integer 转换为 String
                        .collect(Collectors.joining(","));
                createRowForAuthor(sheet, data, rowNum++, paperPmid);
            }

            // 自动调整所有列的宽度
            for (int i = 0; i < AUTHOR_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createInstitutionExcel(Set<Institution> setData, String fileName) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(fileName)) {

            SXSSFSheet sheet = workbook.createSheet("Data");

            sheet.trackAllColumnsForAutoSizing();

            // 创建表头
            createHeaders(sheet, INSTITUTION_HEADERS);

            // 填充数据
            int rowNum = 1;
            for (Institution data : setData) {
                String authorName = data.getAuthorSet().stream()
                        .map(Author::getName)
                        .collect(Collectors.joining(","));
                createRowForInstitution(sheet, data, rowNum++, authorName);
            }

            // 自动调整所有列的宽度
            for (int i = 0; i < INSTITUTION_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCountryExcel(Set<Country> setData, String fileName) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(fileName)) {

            SXSSFSheet sheet = workbook.createSheet("Data");

            sheet.trackAllColumnsForAutoSizing();

            // 创建表头
            createHeaders(sheet, COUNTRY_HEADERS);

            // 填充数据
            int rowNum = 1;
            for (Country data : setData) {
                String institutionName = data.getInstitutionSet().stream()
                        .map(Institution::getName)
                        .collect(Collectors.joining(","));
                createRowForCountry(sheet, data, rowNum++, institutionName);
            }

            // 自动调整所有列的宽度
            for (int i = 0; i < COUNTRY_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void createRowForInstitution(Sheet sheet, Institution data, int rowNum, String authorName) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(data.getId()!=null? String.valueOf(data.getId()):String.valueOf(rowNum));
        row.createCell(1).setCellValue(data.getName());
        row.createCell(2).setCellValue(""); // CITES
        row.createCell(3).setCellValue(authorName);
    }

    private static void createRowForCountry(Sheet sheet, Country data, int rowNum, String institutionName) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(data.getId()!=null? String.valueOf(data.getId()):"");
        row.createCell(1).setCellValue(data.getNation());
        row.createCell(2).setCellValue(""); // CITES
        row.createCell(3).setCellValue(institutionName);
    }

    private static void createRowForAuthor(Sheet sheet, Author data, int rowNum, String paperPmid) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(data.getId()!=null? String.valueOf(data.getId()):"");
        row.createCell(1).setCellValue(data.getName());
        row.createCell(2).setCellValue(data.getNationality());
        row.createCell(3).setCellValue(""); // CITES
        row.createCell(4).setCellValue(paperPmid);
    }

    private static void createRowForPaper(Sheet sheet, Paper data, int rowNum, String citedPmid) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(data.getId() != null ? String.valueOf(data.getId()) : "");
        row.createCell(1).setCellValue(data.getPmid());
        row.createCell(2).setCellValue(data.getTitle() != null ? data.getTitle() : "");
        row.createCell(3).setCellValue(data.getDate() != null ? data.getDate() : "");
        row.createCell(4).setCellValue(data.getKeyword() != null ? data.getKeyword() : "");
        row.createCell(5).setCellValue(data.getJournal() != null ? data.getJournal() : "");
        row.createCell(6).setCellValue(""); // CITES
        row.createCell(7).setCellValue(citedPmid);
    }

    private static void createHeaders(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }
}
