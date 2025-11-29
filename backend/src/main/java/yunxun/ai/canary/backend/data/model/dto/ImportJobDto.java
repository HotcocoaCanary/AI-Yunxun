package yunxun.ai.canary.backend.data.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobDto {
    private String id;
    private String type; // file | text | url
    private String status; // PENDING | SUCCESS | FAILED
    private String errorMessage;
}
