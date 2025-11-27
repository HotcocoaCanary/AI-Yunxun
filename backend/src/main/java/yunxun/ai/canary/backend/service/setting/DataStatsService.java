package yunxun.ai.canary.backend.service.setting;

import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.setting.DataStatsDto;
import yunxun.ai.canary.backend.model.dto.setting.ImportJobDto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataStatsService {

    private final AtomicLong mongoRawDocs = new AtomicLong(42);
    private final AtomicLong mongoAnalysisDocs = new AtomicLong(12);
    private final AtomicLong neo4jNodes = new AtomicLong(128);
    private final AtomicLong neo4jRelations = new AtomicLong(256);

    public DataStatsDto getStats() {
        return DataStatsDto.builder()
                .mongoRawDocuments(mongoRawDocs.get())
                .mongoAnalysisDocuments(mongoAnalysisDocs.get())
                .neo4jNodes(neo4jNodes.get())
                .neo4jRelations(neo4jRelations.get())
                .lastUpdateTime(DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.now()))
                .build();
    }

    public ImportJobDto simulateImport(String type) {
        // 简单自增，模拟数据累积
        mongoRawDocs.incrementAndGet();
        mongoAnalysisDocs.incrementAndGet();
        neo4jNodes.addAndGet(2);
        neo4jRelations.addAndGet(4);
        return ImportJobDto.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .status("SUCCESS")
                .build();
    }
}
