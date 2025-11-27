package yunxun.ai.canary.backend.model.dto.setting;

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
