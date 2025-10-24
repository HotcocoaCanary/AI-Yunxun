package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 查询请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequestDTO {
    
    @NotBlank(message = "查询内容不能为空")
    private String query;
    
    private String type = "general";
    
    @Min(value = 1, message = "topK必须大于0")
    @Max(value = 50, message = "topK不能超过50")
    private Integer topK = 5;
    
    private Double threshold = 0.7;
}
