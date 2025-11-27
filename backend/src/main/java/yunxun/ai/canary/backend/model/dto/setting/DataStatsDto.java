package yunxun.ai.canary.backend.model.dto.setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStatsDto {
    private long mongoRawDocuments;
    private Long mongoAnalysisDocuments;
    private long neo4jNodes;
    private long neo4jRelations;
    private String lastUpdateTime;
}
