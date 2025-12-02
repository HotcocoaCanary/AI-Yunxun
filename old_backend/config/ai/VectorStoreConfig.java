package yunxun.ai.canary.backend.config.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * RAG 管线使用的向量存储配置。
 */
@Configuration
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    @Value("${app.rag.chroma.base-url:http://localhost:8000}")
    private String chromaBaseUrl;

    @Value("${app.rag.chroma.collection:ai-yunxun}")
    private String collectionName;

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        try {
            ChromaApi chromaApi = ChromaApi.builder()
                    .baseUrl(chromaBaseUrl)
                    .build();

            return ChromaVectorStore.builder(chromaApi, embeddingModel)
                    .collectionName(collectionName)
                    .initializeSchema(true)
                    .initializeImmediately(true)
                    .build();
        } catch (Exception ex) {
            log.warn("Chroma vector store unavailable ({}). Falling back to SimpleVectorStore.", ex.getMessage());
            return SimpleVectorStore.builder(embeddingModel).build();
        }
    }
}
