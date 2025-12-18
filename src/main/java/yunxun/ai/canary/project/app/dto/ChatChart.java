package yunxun.ai.canary.project.app.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record ChatChart(
        String type,
        JsonNode option
) {
}

