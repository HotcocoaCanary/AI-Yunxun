package yunxun.ai.canary.backend.user.repository.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import yunxun.ai.canary.backend.user.model.entity.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
