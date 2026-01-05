package mcp.canary.client.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import lombok.NonNull;
import mcp.canary.client.model.ToolLogEvent;
import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class McpLoggingCustomizer implements McpAsyncClientCustomizer {

    private final ApplicationEventPublisher eventPublisher;

    public McpLoggingCustomizer(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void customize(String serverConfigurationName, McpClient.AsyncSpec spec) {
        spec.loggingConsumer(notification -> publish(serverConfigurationName, notification));
    }

    private Mono<Void> publish(String serverName, LoggingMessageNotification notification) {
        return getVoidMono(serverName, notification, eventPublisher);
    }

    @NonNull
    public static Mono<Void> getVoidMono(String serverName, LoggingMessageNotification notification, ApplicationEventPublisher eventPublisher) {
        if (notification == null) {
            return Mono.empty();
        }
        ToolLogEvent event = new ToolLogEvent(
                serverName,
                notification.level() != null ? notification.level().name() : "INFO",
                notification.logger(),
                notification.data(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);
        return Mono.empty();
    }
}
