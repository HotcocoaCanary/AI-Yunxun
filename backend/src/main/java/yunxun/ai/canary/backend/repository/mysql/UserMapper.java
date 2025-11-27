package yunxun.ai.canary.backend.repository.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yunxun.ai.canary.backend.model.entity.user.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
