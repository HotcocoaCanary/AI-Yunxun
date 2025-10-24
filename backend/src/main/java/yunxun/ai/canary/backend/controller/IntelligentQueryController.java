package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.model.dto.ApiResponseDTO;
import yunxun.ai.canary.backend.model.dto.IntelligentQueryResponseDTO;
import yunxun.ai.canary.backend.model.entity.User;
import yunxun.ai.canary.backend.service.IntelligentQueryService;

import java.util.Map;

/**
 * 智能查询控制器 - 提供自然语言查询接口
 * 用户只需要输入自然语言，就能获得图谱和数据分析
 */
@RestController
@RequestMapping("/api/intelligent")
@RequiredArgsConstructor
@Slf4j
public class IntelligentQueryController {
    
    private final IntelligentQueryService intelligentQueryService;
    
    /**
     * 智能自然语言查询
     * 这是主要的查询接口，用户输入自然语言，系统自动分析并返回结果
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponseDTO<IntelligentQueryResponseDTO>> intelligentQuery(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.failure("查询内容不能为空"));
            }
            
            User user = (User) authentication.getPrincipal();
            log.info("收到智能查询请求: user={}, query={}", user.getUsername(), query);
            
            ApiResponseDTO<IntelligentQueryResponseDTO> response = 
                    intelligentQueryService.intelligentQuery(query, user.getId().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("智能查询失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取查询建议
     * 为用户提供查询建议和示例
     */
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getQuerySuggestions() {
        try {
            Map<String, Object> suggestions = Map.of(
                    "examples", new String[]{
                            "查找与机器学习相关的所有论文",
                            "分析深度学习领域的发展趋势",
                            "总结自然语言处理的最新进展",
                            "显示人工智能领域的合作网络",
                            "比较不同算法的性能表现"
                    },
                    "categories", new String[]{
                            "图谱查询",
                            "数据分析", 
                            "文献综述",
                            "趋势分析",
                            "合作网络"
                    },
                    "tips", new String[]{
                            "使用具体的关键词可以获得更精确的结果",
                            "可以指定时间范围，如'近5年'、'2020-2024年'",
                            "可以指定领域，如'计算机科学'、'人工智能'",
                            "可以询问关系，如'谁与谁合作'、'什么引用了什么'"
                    }
            );
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取建议成功", suggestions));
            
        } catch (Exception e) {
            log.error("获取查询建议失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("获取建议失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取查询历史
     * 显示用户的查询历史记录
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getQueryHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("获取查询历史: user={}, page={}, size={}", user.getUsername(), page, size);
            
            // 这里应该调用服务获取查询历史
            // 为了简化示例，返回空结果
            Map<String, Object> history = Map.of(
                    "queries", new Object[0],
                    "total", 0,
                    "page", page,
                    "size", size
            );
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取历史成功", history));
            
        } catch (Exception e) {
            log.error("获取查询历史失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("获取历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取系统状态
     * 显示系统运行状态和统计信息
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getSystemStatus() {
        try {
            Map<String, Object> status = Map.of(
                    "system", "running",
                    "version", "1.0.0",
                    "uptime", System.currentTimeMillis(),
                    "statistics", Map.of(
                            "totalQueries", 0,
                            "totalPapers", 0,
                            "totalEntities", 0,
                            "totalRelationships", 0
                    )
            );
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取状态成功", status));
            
        } catch (Exception e) {
            log.error("获取系统状态失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("获取状态失败: " + e.getMessage()));
        }
    }
}
