package yunxun.ai.canary.backend.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一API响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String error;
    private Long timestamp;
    
    
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    public static <T> ApiResponseDTO<T> success(String message) {
        return success(message, null);
    }
    
    public static <T> ApiResponseDTO<T> failure(String message) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    public static <T> ApiResponseDTO<T> failure(String message, String error) {
        ApiResponseDTO<T> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(error);
        return response;
    }
}
