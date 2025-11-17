package yunxun.ai.canary.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yunxun.ai.canary.backend.model.dto.intelligence.IntelligentQueryPayload;
import yunxun.ai.canary.backend.model.dto.intelligence.IntelligentQueryRequest;
import yunxun.ai.canary.backend.model.dto.intelligence.IntelligentQueryResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IntelligenceController {

    @PostMapping("/intelligent/query")
    public ResponseEntity<IntelligentQueryResponse> query(@Valid @RequestBody IntelligentQueryRequest request) {
        String question = request.getQuery();
        Map<String, Object> chartData = Map.of(
                "type", "bar",
                "title", "Sample distribution",
                "xAxis", List.of("A", "B", "C", "D"),
                "yAxis", List.of(12, 18, 23, 15)
        );

        Map<String, Object> visualizationData = new HashMap<>();
        visualizationData.put("elements", List.of(
                Map.of("data", Map.of("id", "node-1", "label", question)),
                Map.of("data", Map.of("id", "node-2", "label", "Knowledge Graph")),
                Map.of("data", Map.of("id", "node-3", "label", "Context Window")),
                Map.of("data", Map.of("source", "node-1", "target", "node-2", "label", "related")),
                Map.of("data", Map.of("source", "node-2", "target", "node-3", "label", "references"))
        ));

        IntelligentQueryPayload payload = IntelligentQueryPayload.builder()
                .analysisReport("Auto-generated summary for \"" + question + "\" at " + Instant.now() + ".")
                .chartData(chartData)
                .visualizationData(visualizationData)
                .build();

        IntelligentQueryResponse response = IntelligentQueryResponse.builder()
                .success(true)
                .data(payload)
                .message("ok")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/data/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> body = new HashMap<>();
        body.put("fileName", file.getOriginalFilename());
        body.put("size", file.getSize());
        body.put("uploadId", UUID.randomUUID().toString());
        body.put("success", true);
        return ResponseEntity.ok(body);
    }
}
