package yunxun.ai.canary.backend.setting.repository.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yunxun.ai.canary.backend.setting.model.entity.ConfigEntryEntity;

@Mapper
public interface ConfigEntryMapper extends BaseMapper<ConfigEntryEntity> {
}
