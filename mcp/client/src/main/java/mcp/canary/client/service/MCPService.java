package mcp.canary.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import mcp.canary.client.model.McpServerDefinition;
import mcp.canary.client.model.McpServersConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * MCP servers configuration management (Claude Desktop mcpServers format).
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Prefer external file from {@code mcp.servers.file} (default ./mcp-servers.json)</li>
 *   <li>If external file missing, read classpath {@code mcp-servers.json}</li>
 *   <li>Writes always go to external file</li>
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

    public synchronized McpServersConfig readConfig() {
        return readConfigInternal();
    }

    public synchronized Map<String, McpServerDefinition> listServers() {
        return new LinkedHashMap<>(readConfigInternal().mcpServers());
    }

    public synchronized McpServersConfig replaceConfig(McpServersConfig config) {
        McpServersConfig normalized = normalizeConfig(config);
        writeConfigInternal(normalized);
        return normalized;
    }

    public synchronized McpServerDefinition upsertServer(String name, McpServerDefinition definition) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("server name is required");
        }
        McpServerDefinition normalized = normalizeDefinition(definition);

        McpServersConfig current = readConfigInternal();
        Map<String, McpServerDefinition> updated = new LinkedHashMap<>(current.mcpServers());
        updated.put(name, normalized);

        writeConfigInternal(new McpServersConfig(updated));
        return normalized;
    }

    public synchronized Optional<McpServerDefinition> deleteServer(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        McpServersConfig current = readConfigInternal();
        Map<String, McpServerDefinition> updated = new LinkedHashMap<>(current.mcpServers());
        McpServerDefinition removed = updated.remove(name);
        if (removed != null) {
            writeConfigInternal(new McpServersConfig(updated));
            return Optional.of(removed);
        }
        return Optional.empty();
    }

    private McpServersConfig readConfigInternal() {
        if (Files.exists(serversFile)) {
            try {
                byte[] bytes = Files.readAllBytes(serversFile);
                if (bytes.length == 0) {
                    return new McpServersConfig(null);
                }
                return normalizeConfig(objectMapper.readValue(bytes, McpServersConfig.class));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read servers file: " + serversFile, e);
            }
        }

        try {
            ClassPathResource resource = new ClassPathResource("mcp-servers.json");
            if (!resource.exists()) {
                return new McpServersConfig(null);
            }
            return normalizeConfig(objectMapper.readValue(resource.getInputStream(), McpServersConfig.class));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read classpath mcp-servers.json", e);
        }
    }

    private void writeConfigInternal(McpServersConfig config) {
        try {
            Files.createDirectories(serversFile.getParent());
            byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(config);
            Files.write(serversFile, bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write servers file: " + serversFile, e);
        }
    }

    private McpServersConfig normalizeConfig(McpServersConfig config) {
        if (config == null || config.mcpServers() == null) {
            return new McpServersConfig(null);
        }
        return new McpServersConfig(config.mcpServers());
    }

    private McpServerDefinition normalizeDefinition(McpServerDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("server definition is required");
        }
        if (definition.command() == null || definition.command().isBlank()) {
            throw new IllegalArgumentException("server command is required");
        }
        return new McpServerDefinition(definition.command(), definition.args(), definition.env());
    }
}
