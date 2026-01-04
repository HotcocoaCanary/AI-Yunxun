package mcp.canary.client.dto;

/**
 * SSE 输出事件的统一结构（前端可按 type 分发）。
 * <p>
 * 注意：SSE 会分多条发送，因此这里代表“一个事件”。
 */
public record ChatResponse(String type, String content) {
}





