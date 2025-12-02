package yunxun.ai.canary.backend.setting.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import yunxun.ai.canary.backend.data.handler.ResourceHandler;
import yunxun.ai.canary.backend.data.model.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceHandler resourceHandler;

    @PostMapping
    public DataResourceDto create(@RequestParam(required = false) Long userId,
                                  @RequestBody DataResourceCreateRequest request) {
        return resourceHandler.create(resolveUserId(userId), request);
    }

    @GetMapping
    public List<DataResourceDto> list(@RequestParam(required = false) Long userId,
                                      DataResourceQuery query) {
        return resourceHandler.list(resolveUserId(userId), query);
    }

    @PatchMapping("/{id}")
    public DataResourceDto update(@PathVariable String id,
                                  @RequestParam(required = false) Long userId,
                                  @RequestBody DataResourceUpdateRequest request) {
        return resourceHandler.update(resolveUserId(userId), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, @RequestParam(required = false) Long userId) {
        resourceHandler.delete(resolveUserId(userId), id);
    }

    @PostMapping("/{id}/grant")
    public void grant(@PathVariable String id,
                      @RequestParam(required = false) Long userId,
                      @RequestBody DataGrantRequest request) {
        resourceHandler.grant(resolveUserId(userId), id, request);
    }

    private Long resolveUserId(Long userId) {
        if (userId != null) {
            return userId;
        }
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long principal) {
            return principal;
        }
        return 1L;
    }
}
