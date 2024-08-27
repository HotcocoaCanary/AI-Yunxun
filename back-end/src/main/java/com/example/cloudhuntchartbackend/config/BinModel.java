package com.example.cloudhuntchartbackend.config;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class BinModel {

    @Bean
    public SentenceModel sentenceModel() throws IOException {
        return new SentenceModel(new File("bin/en-sent.bin"));
    }
    @Bean
    public ParserModel parserModel() throws IOException {
        return new ParserModel(new File("bin/en-parser-chunking.bin"));
    }
    @Bean
    public TokenizerModel tokenizerModel() throws IOException {
        return new TokenizerModel(new File("bin/en-token.bin"));
    }

    @Bean("personNameFinderModel")
    public TokenNameFinderModel personNameFinderModel() throws IOException {
        return new TokenNameFinderModel(new File("bin/en-ner-person.bin"));
    }

    @Bean("locationNameFinderModel")
    public TokenNameFinderModel locationNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File("bin/en-ner-location.bin"));
    }
    @Bean("organizationNameFinderModel")
    public TokenNameFinderModel organizationNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File("bin/en-ner-organization.bin"));
    }
    @Bean("moneyNameFinderModel")
    public TokenNameFinderModel moneyNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File("bin/en-ner-money.bin"));
    }
    @Bean("dateNameFinderModel")
    public TokenNameFinderModel dateNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File("bin/en-ner-date.bin"));
    }
    @Bean("timeNameFinderModel")
    public TokenNameFinderModel timeNameFinderModel  () throws IOException {
        return new TokenNameFinderModel(new File("bin/en-ner-time.bin"));
    }

}
