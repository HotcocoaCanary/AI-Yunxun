package com.example.cloudhuntchartbackend.utils;

import com.example.cloudhuntchartbackend.entity.Author;
import com.example.cloudhuntchartbackend.entity.Country;
import com.example.cloudhuntchartbackend.entity.Institution;
import com.example.cloudhuntchartbackend.entity.Paper;
import lombok.Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Data
public class NbibToData {
    private Map<Integer, Paper> paperMap = new HashMap<>();
    private Map<String, Author> authorMap = new HashMap<>();
    private Map<String, Institution> institutionMap = new HashMap<>();
    private Map<String, Country> countryMap = new HashMap<>();

    public Map<String, Set<?>> loadFiles(String adPath, String depPath, String fauPath, String jtPath, String mhPath, String tiPath) {
        try {
            loadInstitutionsAndCountries(adPath);
            loadPublicationDates(depPath);
            loadAuthorsAndAffiliations(fauPath);
            loadJournals(jtPath);
            loadKeywords(mhPath);
            loadTitles(tiPath);
            return getDataMap();
        } catch (IOException e) {
            System.err.println("An error occurred while loading files: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Set<?>> getDataMap() {
        Set<Paper> paperSet = new HashSet<>(paperMap.values());
        Set<Author> authorSet = new HashSet<>(authorMap.values());
        Set<Institution> institutionSet = new HashSet<>(institutionMap.values());
        Set<Country> countrySet = new HashSet<>(countryMap.values());

        // 将 Set 集合放入 map 中返回
        Map<String, Set<?>> dataMap = new HashMap<>();
        dataMap.put("paper", paperSet);
        dataMap.put("author", authorSet);
        dataMap.put("institution", institutionSet);
        dataMap.put("country", countrySet);
        return dataMap;
    }

    private void loadInstitutionsAndCountries(String adPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(adPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 使用正则表达式分割字符串，更准确地处理数据格式
                String[] str = line.split("[:,.]");

                // 提取并处理PMID，若格式不正确则跳过
                Integer pmid=Integer.parseInt(str[0].trim());
                String institutionName = str[1].trim();
                String countryName = str[2].trim();

                // 根据国家名获取或创建国家对象// 根据机构名获取或创建机构对象// 根据PMID获取或创建论文对象
                Country country = countryMap.computeIfAbsent(countryName, Country::new);
                Institution institution = institutionMap.computeIfAbsent(institutionName, Institution::new);
                Paper paper = paperMap.computeIfAbsent(pmid, Paper::new);

                country.getInstitutionSet().add(institution);
            }
        }
    }

    private void loadPublicationDates(String depPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(depPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length < 2) continue;

                Integer pmid = Integer.parseInt(parts[0].trim());
                String date = parts[1].trim();

                Paper paper = paperMap.computeIfAbsent(pmid, Paper::new);

                paper.setDate(date);
            }
        }
    }

    private void loadAuthorsAndAffiliations(String fauPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fauPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] str = line.split(":");
                if (str.length < 2) continue;

                Integer pmid = Integer.parseInt(str[0].trim());
                String authorName = str[1];
                Paper paper = paperMap.computeIfAbsent(pmid, Paper::new);

                String[] names = authorName.split(",");
                names = Arrays.stream(names)
                        .filter(name -> !name.trim().isEmpty()) // 过滤掉空字符串和只包含空格的字符串
                        .toArray(String[]::new);
                for (String name : names) {
                    Author author = authorMap.computeIfAbsent(name.trim(), Author::new);
                    author.getPaperSet().add(paper);
                }

                if (str.length >= 3) {
                    String institutionName = str[2];
                    Institution institution = institutionMap.computeIfAbsent(institutionName.trim(), Institution::new);
                    for (String name : names) {
                        Author author = authorMap.get(name.trim());
                        institution.getAuthorSet().add(author);
                    }
                }

                if (str.length == 4) {
                    String countryName = str[3];
                    String institutionName = str[2];
                    Institution institution = institutionMap.computeIfAbsent(institutionName.trim(), Institution::new);
                    Country country = countryMap.computeIfAbsent(countryName.trim(), Country::new);
                    for (String name : names) {
                        Author author = authorMap.get(name.trim());
                        author.setNationality(countryName);
                    }
                    country.getInstitutionSet().add(institution);
                }
            }
        }
    }

    private void loadJournals(String jtPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(jtPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] str = line.split(":");

                Integer pmid = Integer.parseInt(str[0].trim());

                Paper paper = paperMap.computeIfAbsent(pmid, Paper::new);

                paper.setJournal(str[1].trim());
            }
        }
    }

    private void loadKeywords(String mhPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(mhPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] str = line.split(":");

                Integer pmid = Integer.parseInt(str[0].trim());

                Paper paper = paperMap.computeIfAbsent(pmid, Paper::new);

                paper.setKeyword(str[1].trim());
            }
        }
    }

    private void loadTitles(String tiPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(tiPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] str = line.split(":");

                Integer pmid = Integer.parseInt(str[0].trim());

                Paper paper = paperMap.computeIfAbsent(pmid, Paper::new);

                paper.setTitle(str[1].trim());
            }
        }
    }
}