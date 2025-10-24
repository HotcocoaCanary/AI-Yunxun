package yunxun.ai.canary.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定时任务服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {
    
    private final DataProcessingService dataProcessingService;
    
    /**
     * 每小时处理待处理的论文
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void processPendingPapers() {
        log.info("开始处理待处理论文");
        try {
            dataProcessingService.processPendingPapers();
        } catch (Exception e) {
            log.error("处理待处理论文失败", e);
        }
    }
    
    /**
     * 每天凌晨2点清理过期缓存
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredCache() {
        log.info("开始清理过期缓存");
        // 这里可以添加清理Redis缓存的逻辑
    }
}
