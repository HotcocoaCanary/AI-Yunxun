package yunxun.ai.canary.backend.config.ai;

import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * RAG 管线使用的向量存储配置。
 */
@Configuration
public class VectorStoreConfig {

    @Value("${app.rag.chroma.base-url:http://localhost:8000}")
    private String chromaBaseUrl;

    @Value("${app.rag.chroma.collection:ai-yunxun}")
    private String collectionName;

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        ChromaApi chromaApi = ChromaApi.builder()
                .baseUrl(chromaBaseUrl)
                .build();

        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(true)
                .initializeImmediately(true)
                .build();
    }
}
