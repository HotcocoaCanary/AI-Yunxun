package yunxun.ai.canary.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "academic")
public class AcademicProperties {
    private Crawler crawler;
    private Llm llm;
    private Rag rag;

    @Data
    public static class Crawler {
        private Source arxiv;
        private Source cnki;

        @Data
        public static class Source {
            private String baseUrl;
            private int maxPapers;
        }
    }

    @Data
    public static class Llm {
        private ModelConfig entityExtraction;
        private ModelConfig relationExtraction;

        @Data
        public static class ModelConfig {
            private String model;
            private double temperature;
        }
    }

    @Data
    public static class Rag {
        private int chunkSize;
        private int chunkOverlap;
        private int topK;
    }
}
