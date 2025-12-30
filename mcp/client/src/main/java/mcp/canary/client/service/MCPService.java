package mcp.canary.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mcp.canary.client.model.MCPServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MCP 服务器配置管理（读写 JSON）。
 *
 * <p>约定：</p>
 * <ul>
 *   <li>优先读取 {@code mcp.servers.file} 指定的外部文件（默认 ./mcp-servers.json）</li>
 *   <li>若外部文件不存在，则从 classpath 的 {@code mcp-servers.json} 读取默认配置</li>
 *   <li>写入始终写到外部文件，避免打包后无法修改 classpath 资源的问题</li>
 * </ul>
 */
@Service
public class MCPService {

    private final ObjectMapper objectMapper;
    private final Path serversFile;

    public MCPService(ObjectMapper objectMapper,
                      @Value("${mcp.servers.file:./mcp-servers.json}") String serversFile) {
        this.objectMapper = objectMapper;
        this.serversFile = Path.of(serversFile).toAbsolutePath().normalize();
    }

    public synchronized List<MCPServerConfig> listServers() {
        return new ArrayList<>(readServersInternal());
    }

    public synchronized MCPServerConfig addServer(MCPServerConfig server) {
        if (server == null) {
            throw new IllegalArgumentException("server is null");
        }
        if (server.name() == null || server.name().isBlank()) {
            throw new IllegalArgumentException("server.name is required");
        }
        if (server.url() == null || server.url().isBlank()) {
            throw new IllegalArgumentException("server.url is required");
        }
        String protocol = (server.protocol() == null || server.protocol().isBlank()) ? "SSE" : server.protocol();
        MCPServerConfig normalized = new MCPServerConfig(server.id(), server.name(), server.url(), protocol);

        List<MCPServerConfig> servers = readServersInternal();
        boolean exists = servers.stream().anyMatch(s -> s.id() != null && s.id().equals(normalized.id()));
        if (exists) {
            throw new IllegalStateException("server already exists: " + normalized.id());
        }
        servers.add(normalized);
        writeServersInternal(servers);
        return normalized;
    }

    public synchronized Optional<MCPServerConfig> deleteServer(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        List<MCPServerConfig> servers = readServersInternal();
        MCPServerConfig removed = null;
        for (int i = 0; i < servers.size(); i++) {
            MCPServerConfig s = servers.get(i);
            if (id.equals(s.id())) {
                removed = s;
                servers.remove(i);
                break;
            }
        }
        if (removed != null) {
            writeServersInternal(servers);
            return Optional.of(removed);
        }
        return Optional.empty();
    }

    private List<MCPServerConfig> readServersInternal() {
        // 1) 外部文件优先
        if (Files.exists(serversFile)) {
            try {
                byte[] bytes = Files.readAllBytes(serversFile);
                if (bytes.length == 0) {
                    return new ArrayList<>();
                }
                return objectMapper.readValue(bytes, new TypeReference<List<MCPServerConfig>>() {});
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read servers file: " + serversFile, e);
            }
        }

        // 2) fallback：classpath
        try {
            ClassPathResource resource = new ClassPathResource("mcp-servers.json");
            if (!resource.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<MCPServerConfig>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read classpath mcp-servers.json", e);
        }
    }

    private void writeServersInternal(List<MCPServerConfig> servers) {
        try {
            Files.createDirectories(serversFile.getParent());
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(servers);
            Files.write(serversFile, bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write servers file: " + serversFile, e);
        }
    }
}



