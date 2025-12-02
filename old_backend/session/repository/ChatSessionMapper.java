package yunxun.ai.canary.backend.session.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yunxun.ai.canary.backend.session.model.entity.ChatSessionEntity;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}
