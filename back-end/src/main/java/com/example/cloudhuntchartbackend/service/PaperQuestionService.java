package com.example.cloudhuntchartbackend.service;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaperQuestionService {

    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;
    private final NameFinderME personFinder;
    private final NameFinderME locationFinder;
    private final NameFinderME organizationFinder;
    private final NameFinderME dateFinder;

    @Autowired
    public PaperQuestionService(SentenceModel sentenceModel,
                                TokenizerModel tokenizerModel,
                                @Qualifier("personNameFinderModel") TokenNameFinderModel personFinderModel,
                                @Qualifier("locationNameFinderModel") TokenNameFinderModel localNameFinderModel,
                                @Qualifier("organizationNameFinderModel") TokenNameFinderModel organizationFinderModel,
                                @Qualifier("dateNameFinderModel") TokenNameFinderModel dateFinderModel) {
        this.sentenceDetector = new SentenceDetectorME(sentenceModel);
        this.tokenizer = new TokenizerME(tokenizerModel);
        this.personFinder = new NameFinderME(personFinderModel);
        this.locationFinder = new NameFinderME(localNameFinderModel);
        this.organizationFinder = new NameFinderME(organizationFinderModel);
        this.dateFinder = new NameFinderME(dateFinderModel);
    }

    public String answer(String question) {
        String[] sentences = sentenceDetector.sentDetect(question);
        Map<String, String> entities = new HashMap<>();

        for (String sentence : sentences) {
            String[] tokens = tokenizer.tokenize(sentence);
            findEntities(tokens, entities, dateFinder, "date");
            findEntities(tokens, entities, personFinder, "person");
            findEntities(tokens, entities, organizationFinder, "organization");
        }

        return generateAnswer(question, entities);
    }

    private void findEntities(String[] tokens, Map<String, String> entities, NameFinderME finder, String entityType) {
        Span[] spans = finder.find(tokens);
        for (Span span : spans) {
            StringBuilder entityBuilder = new StringBuilder();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                entityBuilder.append(tokens[i]).append(" ");
            }
            String entity = entityBuilder.toString().trim();
            entities.putIfAbsent(entityType, entity);
        }
    }


    private String generateAnswer(String question, Map<String, String> entities) {
        String cypherQuery = generateCypherQuery(question, entities);
        // 这里可以选择直接执行查询，或者返回查询语句由调用者执行
        return cypherQuery;
    }

    private String generateCypherQuery(String question, Map<String, String> entities) {
        if (question.toLowerCase().contains("发表时间")) {
            return "MATCH (p:Paper) WHERE p.publicationDate = '" + entities.get("date") + "' RETURN p";
        } else if (question.toLowerCase().contains("作者")) {
            return "MATCH (a:Author)-[:Authored]->(p:Paper) WHERE a.name = '" + entities.get("person") + "' RETURN p";
        } else if (question.toLowerCase().contains("期刊")) {
            return "MATCH (i:Institution)<-[:PublishedIn]-(p:Paper) WHERE i.name = '" + entities.get("organization") + "' RETURN p";
        } else if (question.toLowerCase().contains("国家") || question.toLowerCase().contains("发表机构")) {
            return "MATCH (c:Country)<-[:LOCATED_IN]-(i:Institution)<-[:PublishedIn]-(p:Paper) WHERE c.name = '" + entities.get("organization") + "' RETURN p";
        } else {
            return "对不起，我无法生成Cypher查询语句来回答这个问题";
        }
    }
}
