package com.example.cloudhuntchartbackend.service;

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
    private final NameFinderME locationFinder;
    private final NameFinderME organizationFinder;
    private final NameFinderME dateFinder;

    @Autowired
    public EntityExtractor(SentenceModel sentenceModel,
                           TokenizerModel tokenizerModel,
                           @Qualifier("personNameFinderModel") TokenNameFinderModel personFinderModel,
                           @Qualifier("locationNameFinderModel") TokenNameFinderModel locationFinderModel,
                           @Qualifier("organizationNameFinderModel") TokenNameFinderModel organizationFinderModel,
                           @Qualifier("dateNameFinderModel") TokenNameFinderModel dateFinderModel) {
        this.sentenceDetector = new SentenceDetectorME(sentenceModel);
        this.tokenizer = new TokenizerME(tokenizerModel);
        this.personFinder = new NameFinderME(personFinderModel);
        this.locationFinder = new NameFinderME(locationFinderModel);
        this.organizationFinder = new NameFinderME(organizationFinderModel);
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
            Span[] spans = personFinder.find(tokens);
            Span[] locationSpans = locationFinder.find(tokens);
            Span[] organizationSpans = organizationFinder.find(tokens);
            Span[] dateSpans = dateFinder.find(tokens);
            for (Span span : spans) {
                String entity = tokens[span.getStart()];
                attributes.put("Author", entity);
            }
            for (Span span : locationSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Institution", entity);
            }
            for (Span span : organizationSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Organization", entity);
            }
            for (Span span : organizationSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Organization", entity);
            }
            for (Span span : dateSpans) {
                String entity = tokens[span.getStart()];
                attributes.put("Date", entity);
            }
        }
        return attributes;
    }
}
