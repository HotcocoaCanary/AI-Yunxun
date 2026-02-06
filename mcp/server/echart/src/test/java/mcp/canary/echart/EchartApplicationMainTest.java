package mcp.canary.echart;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EchartApplicationMainTest {

    @Test
    void main_startsAndStops() {
        assertDoesNotThrow(() -> EchartApplication.main(new String[]{
                "--spring.main.web-application-type=none",
                "--spring.main.banner-mode=off",
                "--spring.main.register-shutdown-hook=false"
        }));
    }
}
