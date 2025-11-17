package yunxun.ai.canary.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import yunxun.ai.canary.backend.config.TestAiConfiguration;

@SpringBootTest
@Import(TestAiConfiguration.class)
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
