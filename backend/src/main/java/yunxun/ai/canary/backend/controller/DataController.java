package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yunxun.ai.canary.backend.model.dto.ApiResponseDTO;
import yunxun.ai.canary.backend.model.entity.User;
import yunxun.ai.canary.backend.service.DataProcessingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据管理控制器
 * 处理数据上传、下载、导入导出等功能
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {
    
    private final DataProcessingService dataProcessingService;
    
    /**
     * 上传数据文件
     * 支持JSON、CSV、Excel格式的三元组数据
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> uploadData(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("收到数据上传请求: user={}, filename={}, size={}", 
                    user.getUsername(), file.getOriginalFilename(), file.getSize());
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("application/json") && 
                !contentType.equals("text/csv") && 
                !contentType.equals("application/vnd.ms-excel") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.failure("不支持的文件格式，请上传JSON、CSV或Excel文件"));
            }
            
            // 处理文件上传
            Map<String, Object> result = dataProcessingService.processUploadedFile(file, user.getId().toString());
            
            return ResponseEntity.ok(ApiResponseDTO.success("文件上传成功", result));
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("文件上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 下载数据文件
     * 根据查询条件导出三元组数据
     */
    @PostMapping("/download")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> downloadData(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("收到数据下载请求: user={}, request={}", user.getUsername(), request);
            
            // 根据查询条件生成下载数据
            Map<String, Object> result = dataProcessingService.generateDownloadData(request, user.getId().toString());
            
            return ResponseEntity.ok(ApiResponseDTO.success("数据导出成功", result));
            
        } catch (Exception e) {
            log.error("数据下载失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("数据下载失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取用户上传的文件列表
     */
    @GetMapping("/files")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getUserFiles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("获取用户文件列表: user={}, page={}, size={}", user.getUsername(), page, size);
            
            // 获取用户文件列表
            Map<String, Object> files = dataProcessingService.getUserFiles(user.getId().toString(), page, size);
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取文件列表成功", files));
            
        } catch (Exception e) {
            log.error("获取文件列表失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("获取文件列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> deleteFile(
            @PathVariable String fileId,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("删除文件请求: user={}, fileId={}", user.getUsername(), fileId);
            
            // 删除文件
            dataProcessingService.deleteFile(fileId, user.getId().toString());
            
            return ResponseEntity.ok(ApiResponseDTO.success("文件删除成功", new HashMap<>()));
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("删除文件失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取数据统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getDataStatistics(
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("获取数据统计: user={}", user.getUsername());
            
            // 获取统计信息
            Map<String, Object> statistics = dataProcessingService.getDataStatistics(user.getId().toString());
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取统计信息成功", statistics));
            
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("获取统计信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量导入三元组数据
     */
    @PostMapping("/import-triples")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> importTriples(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("批量导入三元组: user={}", user.getUsername());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> triples = (List<Map<String, Object>>) request.get("triples");
            if (triples == null || triples.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.failure("三元组数据不能为空"));
            }
            
            // 导入三元组到Neo4j
            Map<String, Object> result = dataProcessingService.importTriples(triples, user.getId().toString());
            
            return ResponseEntity.ok(ApiResponseDTO.success("三元组导入成功", result));
            
        } catch (Exception e) {
            log.error("导入三元组失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("导入三元组失败: " + e.getMessage()));
        }
    }
    
    /**
     * 导出三元组数据
     */
    @PostMapping("/export-triples")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> exportTriples(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            User user = (User) authentication.getPrincipal();
            log.info("导出三元组: user={}", user.getUsername());
            
            // 根据查询条件导出三元组
            Map<String, Object> result = dataProcessingService.exportTriples(request, user.getId().toString());
            
            return ResponseEntity.ok(ApiResponseDTO.success("三元组导出成功", result));
            
        } catch (Exception e) {
            log.error("导出三元组失败", e);
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.failure("导出三元组失败: " + e.getMessage()));
        }
    }
}
