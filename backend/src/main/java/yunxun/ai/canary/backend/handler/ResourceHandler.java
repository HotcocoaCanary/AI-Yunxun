package yunxun.ai.canary.backend.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yunxun.ai.canary.backend.model.dto.data.*;
import yunxun.ai.canary.backend.service.setting.resource.DataResourceService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceHandler {

    private final DataResourceService dataResourceService;

    public DataResourceDto create(Long userId, DataResourceCreateRequest request) {
        return dataResourceService.create(userId, request);
    }

    public List<DataResourceDto> list(Long userId, DataResourceQuery query) {
        return dataResourceService.list(userId, query);
    }

    public DataResourceDto update(Long userId, String id, DataResourceUpdateRequest request) {
        return dataResourceService.update(userId, id, request);
    }

    public void delete(Long userId, String id) {
        dataResourceService.delete(userId, id);
    }

    public void grant(Long userId, String id, DataGrantRequest request) {
        dataResourceService.grant(userId, id, request);
    }
}