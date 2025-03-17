package com.example.aiyunxun.util;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
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
public class EntityExtractor {

    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;
    private final NameFinderME personFinder;
    private final NameFinderME institutionFinder;
    private final NameFinderME dateFinder;

    @Autowired
    public EntityExtractor(SentenceModel sentenceModel,
                           TokenizerModel tokenizerModel,
                           @Qualifier("personNameFinderModel") TokenNameFinderModel personFinderModel,
                           @Qualifier("institutionNameFinderModel") TokenNameFinderModel institutionFinderModel,
                           @Qualifier("dateNameFinderModel") TokenNameFinderModel dateFinderModel
                           //@Qualifier("countryNameFinderModel") TokenNameFinderModel countryFinderModel,
                           //@Qualifier("journalNameFinderModel") TokenNameFinderModel journalFinderModel,
                           //@Qualifier("keywordNameFinderModel") TokenNameFinderModel keywordFinderModel,
                           //@Qualifier("titleNameFinderModel") TokenNameFinderModel titleFinderModel
                           ){
        this.sentenceDetector =new SentenceDetectorME(sentenceModel);
        this.tokenizer = new TokenizerME(tokenizerModel);
        this.personFinder = new NameFinderME(personFinderModel);
        this.institutionFinder = new NameFinderME(institutionFinderModel);
        this.dateFinder = new NameFinderME(dateFinderModel);
    }

    public Map<String, Object> extractEntityAttributes(String sentence) {
        Map<String, Object> attributes = new HashMap<>();
        // 分句
        String[] sentences = sentenceDetector.sentDetect(sentence);
        for (String sentenceText : sentences) {
            // 分词
            String[] tokens = tokenizer.tokenize(sentenceText);
            // 识别实体
            Span[] personSpans = personFinder.find(tokens);
            Span[] institutionSpans = institutionFinder.find(tokens);
            Span[] dateSpans = dateFinder.find(tokens);

            // 处理每个实体并存储到属性 map 中
            for (Span span : personSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Author", entity);
            }
            for (Span span : institutionSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Institution", entity);
            }
            for (Span span : dateSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Date", entity);
            }
        }
        return attributes;
    }
}
