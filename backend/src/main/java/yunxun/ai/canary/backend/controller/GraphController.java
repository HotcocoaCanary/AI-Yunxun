package yunxun.ai.canary.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yunxun.ai.canary.backend.model.dto.graph.GraphDataDto;
import yunxun.ai.canary.backend.model.dto.graph.GraphExpandRequest;
import yunxun.ai.canary.backend.service.graph.GraphMockService;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphMockService graphMockService;

    @GetMapping("/overview")
    public GraphDataDto overview() {
        return graphMockService.overview();
    }

    @PostMapping("/expand")
    public GraphDataDto expand(@RequestBody GraphExpandRequest request) {
        return graphMockService.expand(request.getNodeId(), request.getLimit());
    }
}
