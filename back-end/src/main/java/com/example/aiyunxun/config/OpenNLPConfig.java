package com.example.aiyunxun.config;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class OpenNLPConfig {

    @Value("${opennlp.model.path}")
    private String modelPath;

    @Bean
    public SentenceModel sentenceModel() throws IOException {
        return new SentenceModel(new File(modelPath+"en-sent.bin"));
    }
    @Bean
    public ParserModel parserModel() throws IOException {
        return new ParserModel(new File(modelPath+"en-parser-chunking.bin"));
    }
    @Bean
    public TokenizerModel tokenizerModel() throws IOException {
        return new TokenizerModel(new File(modelPath+"en-token.bin"));
    }
    @Bean("personNameFinderModel")
    public TokenNameFinderModel personNameFinderModel() throws IOException {
        return new TokenNameFinderModel(new File(modelPath+"en-ner-person.bin"));
    }
    @Bean("locationNameFinderModel")
    public TokenNameFinderModel locationNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File(modelPath+"en-ner-location.bin"));
    }
    @Bean("institutionNameFinderModel")
    public TokenNameFinderModel institutionNameFinderModel() throws IOException {
        return new TokenNameFinderModel(new File(modelPath+"en-ner-institution.bin"));
    }
    @Bean("moneyNameFinderModel")
    public TokenNameFinderModel moneyNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File(modelPath+"en-ner-money.bin"));
    }
    @Bean("dateNameFinderModel")
    public TokenNameFinderModel dateNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File(modelPath+"en-ner-date.bin"));
    }
    @Bean("timeNameFinderModel")
    public TokenNameFinderModel timeNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File(modelPath+"en-ner-time.bin"));
    }
}
