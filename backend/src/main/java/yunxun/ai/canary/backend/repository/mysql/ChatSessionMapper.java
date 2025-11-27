package yunxun.ai.canary.backend.repository.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yunxun.ai.canary.backend.model.entity.chat.ChatSessionEntity;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}
