package yunxun.ai.canary.backend.data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import yunxun.ai.canary.backend.data.model.dto.*;
import yunxun.ai.canary.backend.data.model.entity.DataResourceDoc;
import yunxun.ai.canary.backend.data.repository.DataResourceRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataResourceService {

    private final DataResourceRepository repository;

    public DataResourceDto create(Long userId, DataResourceCreateRequest request) {
        DataResourceDoc doc = DataResourceDoc.builder()
                .ownerId(userId)
                .type(request.getType())
                .title(request.getTitle())
                .visibility(request.getVisibility())
                .allowedNodes(request.getAllowedNodes())
                .meta(request.getMeta())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        DataResourceDoc saved = repository.save(doc);
        return toDto(saved);
    }

    public List<DataResourceDto> list(Long userId, DataResourceQuery query) {
        List<DataResourceDoc> docs = new ArrayList<>();
        if (query != null && "public".equalsIgnoreCase(query.getVisibility())) {
            docs.addAll(repository.findByVisibility("public"));
        } else if (query != null && "all".equalsIgnoreCase(query.getVisibility())) {
            docs.addAll(repository.findByOwnerId(userId));
            docs.addAll(repository.findByVisibility("public"));
        } else {
            docs.addAll(repository.findByOwnerId(userId));
        }
        if (query != null && query.getType() != null) {
            docs = docs.stream()
                    .filter(d -> query.getType().equalsIgnoreCase(d.getType()))
                    .toList();
        }
        return docs.stream().map(this::toDto).collect(Collectors.toList());
    }

    public DataResourceDto update(Long userId, String id, DataResourceUpdateRequest request) {
        DataResourceDoc doc = repository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found or no permission"));
        if (request.getTitle() != null) doc.setTitle(request.getTitle());
        if (request.getVisibility() != null) doc.setVisibility(request.getVisibility());
        if (request.getAllowedNodes() != null) doc.setAllowedNodes(request.getAllowedNodes());
        if (request.getMeta() != null) doc.setMeta(merge(doc.getMeta(), request.getMeta()));
        doc.setUpdatedAt(Instant.now());
        repository.save(doc);
        return toDto(doc);
    }

    public void delete(Long userId, String id) {
        repository.findByIdAndOwnerId(id, userId).ifPresent(repository::delete);
    }

    public void grant(Long userId, String id, DataGrantRequest request) {
        DataResourceDoc doc = repository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found or no permission"));
        if (request.getVisibility() != null) {
            doc.setVisibility(request.getVisibility());
        }
        if (!CollectionUtils.isEmpty(request.getAllowedNodes())) {
            doc.setAllowedNodes(request.getAllowedNodes());
        }
        doc.setUpdatedAt(Instant.now());
        repository.save(doc);
    }

    private DataResourceDto toDto(DataResourceDoc doc) {
        return DataResourceDto.builder()
                .id(doc.getId())
                .ownerId(doc.getOwnerId())
                .type(doc.getType())
                .title(doc.getTitle())
                .visibility(doc.getVisibility())
                .allowedNodes(doc.getAllowedNodes())
                .meta(doc.getMeta())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private Map<String, Object> merge(Map<String, Object> src, Map<String, Object> delta) {
        if (src == null) return delta;
        src.putAll(delta);
        return src;
    }
}