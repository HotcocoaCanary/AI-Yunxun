package com.example.cloudhuntchartbackend.service;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EntityExtractionService {

    @Autowired
    private TokenizerModel tokenizerModel;

    @Autowired
    private TokenNameFinderModel personNameFinderModel;

    @Autowired
    private TokenNameFinderModel locationNameFinderModel;

    @Autowired
    private TokenNameFinderModel organizationNameFinderModel;

    @Autowired
    private TokenNameFinderModel moneyNameFinderModel;

    @Autowired
    private TokenNameFinderModel dateNameFinderModel;

    @Autowired
    private TokenNameFinderModel timeNameFinderModel;

    @Autowired
    private ParserModel parserModel;

    public List<String> extractEntities(String sentence) throws IOException {
        List<String> entities = new ArrayList<>();

        // Tokenize the sentence
        TokenizerME tokenizer = new TokenizerME(tokenizerModel);
        String[] tokens = tokenizer.tokenize(sentence);

        // Extract entities
        NameFinderME personFinder = new NameFinderME(personNameFinderModel);
        Span[] personSpans = personFinder.find(tokens);
        addEntities(tokens, personSpans, entities, "PERSON");

        NameFinderME locationFinder = new NameFinderME(locationNameFinderModel);
        Span[] locationSpans = locationFinder.find(tokens);
        addEntities(tokens, locationSpans, entities, "LOCATION");

        // ... Similarly for organization, money, date, and time

        // Parse the sentence to extract named entities like "author", "paper", "journal"
        InputStream modelStream = new FileInputStream("bin/en-parser-chunking.bin");
        ParserModel parserModel = new ParserModel(modelStream);
        Parser parser = ParserFactory.create(parserModel);
        Parse[] parses = ParserTool.parseLine(sentence, parser, 1);
        for (Parse p : parses) {
            extractNamedEntities(p, entities);
        }

        return entities;
    }

    private void addEntities(String[] tokens, Span[] spans, List<String> entities, String type) {
        for (Span span : spans) {
            String entity = Arrays.toString(Span.spansToStrings(new Span[]{span}, tokens));
            entities.add(type + ": " + entity);
        }
    }

    private void extractNamedEntities(Parse p, List<String> entities) {
        // Extract NP (noun phrase) chunks to find named entities
        Parse[] children = p.getChildren();
        for (Parse child : children) {
            if (child.getType().equals("NP")) {
                String np = child.getCoveredText();
                entities.add("NOUN-PHRASE: " + np);
            }
            extractNamedEntities(child, entities); // Recursively process child nodes
        }
    }
}
