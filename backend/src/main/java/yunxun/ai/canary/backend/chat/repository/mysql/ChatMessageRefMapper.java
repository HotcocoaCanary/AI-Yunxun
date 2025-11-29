package yunxun.ai.canary.backend.chat.repository.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yunxun.ai.canary.backend.chat.model.entity.ChatMessageRefEntity;

@Mapper
public interface ChatMessageRefMapper extends BaseMapper<ChatMessageRefEntity> {
}
