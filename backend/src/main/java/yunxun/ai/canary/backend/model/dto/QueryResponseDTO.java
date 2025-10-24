package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryResponseDTO {
    
    private String query;
    private String cypherQuery;
    private List<Map<String, Object>> graphResults;
    private List<Map<String, Object>> ragResults;
    private String enhancedAnswer;
    private Long executionTime;
    
    public static QueryResponseDTO success(String query, String cypherQuery, 
                                         List<Map<String, Object>> graphResults,
                                         List<Map<String, Object>> ragResults,
                                         String enhancedAnswer, Long executionTime) {
        return QueryResponseDTO.builder()
                .query(query)
                .cypherQuery(cypherQuery)
                .graphResults(graphResults)
                .ragResults(ragResults)
                .enhancedAnswer(enhancedAnswer)
                .executionTime(executionTime)
                .build();
    }
}
