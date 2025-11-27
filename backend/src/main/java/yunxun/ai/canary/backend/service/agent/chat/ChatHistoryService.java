package yunxun.ai.canary.backend.service.agent.chat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yunxun.ai.canary.backend.model.dto.chat.ChatMessageAppendRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatMessageDto;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeCreateRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeRenameRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatTreeNodeDto;
import yunxun.ai.canary.backend.model.entity.chat.ChatMessageDoc;
import yunxun.ai.canary.backend.model.entity.chat.ChatMessageRefEntity;
import yunxun.ai.canary.backend.model.entity.chat.ChatSessionEntity;
import yunxun.ai.canary.backend.repository.mongo.ChatMessageMongoRepository;
import yunxun.ai.canary.backend.repository.mysql.ChatMessageRefMapper;
import yunxun.ai.canary.backend.repository.mysql.ChatSessionMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageRefMapper chatMessageRefMapper;
    private final ChatMessageMongoRepository chatMessageMongoRepository;

    public List<ChatTreeNodeDto> getTree(Long userId) {
        List<ChatSessionEntity> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSessionEntity>()
                        .eq(ChatSessionEntity::getUserId, userId)
                        .orderByAsc(ChatSessionEntity::getPath));

        Map<Long, ChatTreeNodeDto> nodeMap = new HashMap<>();
        List<ChatTreeNodeDto> roots = new ArrayList<>();

        for (ChatSessionEntity session : sessions) {
            ChatTreeNodeDto node = toTreeNode(session);
            nodeMap.put(session.getId(), node);
            if (session.getParentId() == null) {
                roots.add(node);
            } else {
                ChatTreeNodeDto parent = nodeMap.get(session.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node); // fallback to root to avoid losing node
                }
            }
        }
        return roots;
    }

    @Transactional
    public ChatTreeNodeDto createNode(ChatNodeCreateRequest request, Long userId) {
        Long parentId = Optional.ofNullable(request.getParentId()).map(Long::valueOf).orElse(null);
        ChatSessionEntity parent = null;
        if (parentId != null) {
            parent = chatSessionMapper.selectById(parentId);
        }
        LocalDateTime now = LocalDateTime.now();
        ChatSessionEntity entity = ChatSessionEntity.builder()
                .userId(userId)
                .parentId(parentId)
                .type(request.getType())
                .title(Optional.ofNullable(request.getName()).orElse("新建"))
                .isArchived(Boolean.FALSE)
                .metadata("{}")
                .createdAt(now)
                .updatedAt(now)
                .build();
        chatSessionMapper.insert(entity);
        String path = buildPath(parent, entity.getId());
        entity.setPath(path);
        chatSessionMapper.updateById(entity);
        return toTreeNode(entity);
    }

    public ChatTreeNodeDto renameNode(Long nodeId, ChatNodeRenameRequest request, Long userId) {
        ChatSessionEntity entity = chatSessionMapper.selectById(nodeId);
        if (entity == null || !Objects.equals(entity.getUserId(), userId)) {
            return null;
        }
        entity.setTitle(request.getName());
        entity.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.updateById(entity);
        return toTreeNode(entity);
    }

    @Transactional
    public void deleteNode(Long nodeId, Long userId) {
        ChatSessionEntity entity = chatSessionMapper.selectById(nodeId);
        if (entity == null || !Objects.equals(entity.getUserId(), userId)) {
            return;
        }
        entity.setIsArchived(Boolean.TRUE);
        chatSessionMapper.updateById(entity);
    }

    public List<ChatMessageDto> getMessages(Long sessionId) {
        List<ChatMessageRefEntity> refs = chatMessageRefMapper.selectList(
                new LambdaQueryWrapper<ChatMessageRefEntity>()
                        .eq(ChatMessageRefEntity::getSessionId, sessionId)
                        .orderByAsc(ChatMessageRefEntity::getSeq));
        List<String> ids = refs.stream().map(ChatMessageRefEntity::getMessageId).toList();
        Map<String, ChatMessageDoc> docMap = chatMessageMongoRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(ChatMessageDoc::getId, d -> d));

        List<ChatMessageDto> result = new ArrayList<>();
        for (ChatMessageRefEntity ref : refs) {
            ChatMessageDoc doc = docMap.get(ref.getMessageId());
            Instant createdAt = doc != null && doc.getCreatedAt() != null ? doc.getCreatedAt() : Instant.now();
            String content = doc != null && doc.getContent() != null ? doc.getContent().getMarkdown() : "";
            result.add(ChatMessageDto.builder()
                    .id(ref.getMessageId())
                    .role(ref.getRole())
                    .content(content)
                    .createdAt(createdAt)
                    .build());
        }
        return result;
    }

    @Transactional
    public ChatMessageDto appendMessage(ChatMessageAppendRequest request) {
        Instant now = Instant.now();
        ChatMessageDoc.Content content = ChatMessageDoc.Content.builder()
                .markdown(request.getContent())
                .build();
        ChatMessageDoc doc = ChatMessageDoc.builder()
                .sessionId(request.getSessionId())
                .role(request.getRole())
                .content(content)
                .createdAt(now)
                .build();
        ChatMessageDoc saved = chatMessageMongoRepository.save(doc);

        Long seq = chatMessageRefMapper.selectCount(
                new LambdaQueryWrapper<ChatMessageRefEntity>()
                        .eq(ChatMessageRefEntity::getSessionId, request.getSessionId())) + 1;
        ChatMessageRefEntity ref = ChatMessageRefEntity.builder()
                .sessionId(request.getSessionId())
                .messageId(saved.getId())
                .role(request.getRole())
                .seq(seq)
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRefMapper.insert(ref);
        return ChatMessageDto.builder()
                .id(saved.getId())
                .role(request.getRole())
                .content(request.getContent())
                .createdAt(now)
                .build();
    }

    private ChatTreeNodeDto toTreeNode(ChatSessionEntity entity) {
        Instant lastMessageAt = entity.getUpdatedAt() != null ? entity.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        return ChatTreeNodeDto.builder()
                .id(String.valueOf(entity.getId()))
                .type("folder".equals(entity.getType()) ? "group" : entity.getType())
                .name(entity.getTitle())
                .parentId(entity.getParentId() == null ? null : String.valueOf(entity.getParentId()))
                .lastMessageAt(lastMessageAt)
                .build();
    }

    private String buildPath(ChatSessionEntity parent, Long id) {
        if (parent == null || parent.getPath() == null) {
            return String.valueOf(id);
        }
        return parent.getPath() + "/" + id;
    }
}
