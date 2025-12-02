package yunxun.ai.canary.backend.config.db;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
        "yunxun.ai.canary.backend.user.repository.mysql",
        "yunxun.ai.canary.backend.chat.repository.mysql",
        "yunxun.ai.canary.backend.session.repository",
        "yunxun.ai.canary.backend.setting.repository.mysql"
})
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        return new MybatisPlusInterceptor();
    }
}
