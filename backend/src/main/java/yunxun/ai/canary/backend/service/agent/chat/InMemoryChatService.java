package yunxun.ai.canary.backend.service.agent.chat;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.chart.ChartSpecDto;
import yunxun.ai.canary.backend.model.dto.chat.ChatMessageDto;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeCreateRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatNodeRenameRequest;
import yunxun.ai.canary.backend.model.dto.chat.ChatTreeNodeDto;
import yunxun.ai.canary.backend.model.dto.graph.GraphDataDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
@Slf4j
public class InMemoryChatService {

    private final List<ChatTreeNodeDto> tree = new CopyOnWriteArrayList<>();
    private final Map<String, List<ChatMessageDto>> messagesBySession = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!tree.isEmpty()) {
            return;
        }
        ChatTreeNodeDto root = ChatTreeNodeDto.builder()
                .id("group-root")
                .type("group")
                .name("默认分组")
                .parentId(null)
                .children(new ArrayList<>())
                .build();
        ChatTreeNodeDto session = ChatTreeNodeDto.builder()
                .id("session-demo")
                .type("session")
                .name("图谱问答示例")
                .parentId(root.getId())
                .build();
        root.getChildren().add(session);
        tree.add(root);

        messagesBySession.put(session.getId(), new CopyOnWriteArrayList<>());
        appendMessage(session.getId(), "assistant", "欢迎使用 AI-Yunxun，可直接提问或粘贴数据。");
    }

    public List<ChatTreeNodeDto> getTree() {
        return tree;
    }

    public ChatTreeNodeDto createNode(ChatNodeCreateRequest request) {
        String id = request.getType() + "-" + UUID.randomUUID();
        ChatTreeNodeDto node = ChatTreeNodeDto.builder()
                .id(id)
                .type(request.getType())
                .name(Optional.ofNullable(request.getName()).orElse("新建"))
                .parentId(request.getParentId())
                .children(new ArrayList<>())
                .build();
        if (request.getParentId() == null) {
            tree.add(node);
        } else {
            traverse(tree, request.getParentId(), parent -> parent.getChildren().add(node));
        }
        if ("session".equals(request.getType())) {
            messagesBySession.putIfAbsent(id, new CopyOnWriteArrayList<>());
        }
        return node;
    }

    public ChatTreeNodeDto renameNode(String nodeId, ChatNodeRenameRequest request) {
        traverse(tree, nodeId, node -> node.setName(request.getName()));
        return findNode(tree, nodeId).orElseThrow();
    }

    public void deleteNode(String nodeId) {
        List<String> sessionIds = new ArrayList<>();
        collectSessionIds(tree, nodeId, sessionIds);
        removeNode(tree, nodeId);
        sessionIds.forEach(messagesBySession::remove);
    }

    public List<ChatMessageDto> getMessages(String sessionId) {
        return messagesBySession.getOrDefault(sessionId, new ArrayList<>());
    }

    public ChatMessageDto appendMessage(String sessionId, String role, String content) {
        ChatMessageDto message = ChatMessageDto.builder()
                .id(role + "-" + UUID.randomUUID())
                .role(role)
                .content(content)
                .createdAt(Instant.now())
                .build();
        messagesBySession.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(message);
        updateLastMessageAt(sessionId);
        return message;
    }

    public void appendTextToMessage(String sessionId, String messageId, String content) {
        messagesBySession.getOrDefault(sessionId, new ArrayList<>())
                .stream()
                .filter(msg -> Objects.equals(msg.getId(), messageId))
                .findFirst()
                .ifPresent(msg -> msg.setContent(msg.getContent() + content));
    }

    public void attachGraph(String sessionId, String messageId, GraphDataDto graph) {
        messagesBySession.getOrDefault(sessionId, new ArrayList<>())
                .stream()
                .filter(msg -> Objects.equals(msg.getId(), messageId))
                .findFirst()
                .ifPresent(msg -> msg.setGraph(graph));
    }

    public void attachCharts(String sessionId, String messageId, List<ChartSpecDto> charts) {
        messagesBySession.getOrDefault(sessionId, new ArrayList<>())
                .stream()
                .filter(msg -> Objects.equals(msg.getId(), messageId))
                .findFirst()
                .ifPresent(msg -> msg.setCharts(charts));
    }

    private void updateLastMessageAt(String sessionId) {
        Instant now = Instant.now();
        traverse(tree, sessionId, node -> node.setLastMessageAt(now));
    }

    private Optional<ChatTreeNodeDto> findNode(List<ChatTreeNodeDto> nodes, String id) {
        for (ChatTreeNodeDto node : nodes) {
            if (Objects.equals(node.getId(), id)) {
                return Optional.of(node);
            }
            if (node.getChildren() != null) {
                Optional<ChatTreeNodeDto> found = findNode(node.getChildren(), id);
                if (found.isPresent()) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    private void traverse(List<ChatTreeNodeDto> nodes, String targetId, Consumer<ChatTreeNodeDto> consumer) {
        for (ChatTreeNodeDto node : nodes) {
            if (Objects.equals(node.getId(), targetId)) {
                consumer.accept(node);
                return;
            }
            if (node.getChildren() != null) {
                traverse(node.getChildren(), targetId, consumer);
            }
        }
    }

    private boolean removeNode(List<ChatTreeNodeDto> nodes, String targetId) {
        return nodes.removeIf(node -> {
            if (Objects.equals(node.getId(), targetId)) {
                return true;
            }
            if (node.getChildren() != null) {
                return removeNode(node.getChildren(), targetId);
            }
            return false;
        });
    }

    private void collectSessionIds(List<ChatTreeNodeDto> nodes, String targetId, List<String> collector) {
        for (ChatTreeNodeDto node : nodes) {
            if (Objects.equals(node.getId(), targetId)) {
                collectSessions(node, collector);
                return;
            }
            if (node.getChildren() != null) {
                collectSessionIds(node.getChildren(), targetId, collector);
            }
        }
    }

    private void collectSessions(ChatTreeNodeDto node, List<String> collector) {
        if ("session".equals(node.getType())) {
            collector.add(node.getId());
        }
        if (node.getChildren() != null) {
            node.getChildren().forEach(child -> collectSessions(child, collector));
        }
    }
}
