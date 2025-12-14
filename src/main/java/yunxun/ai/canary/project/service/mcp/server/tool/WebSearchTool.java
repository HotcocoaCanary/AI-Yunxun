package yunxun.ai.canary.project.service.mcp.server.tool;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 网络搜索工具
 */
@Component
@ConditionalOnProperty(name = "web.search.enabled", havingValue = "true", matchIfMissing = false)
public class WebSearchTool {

}


