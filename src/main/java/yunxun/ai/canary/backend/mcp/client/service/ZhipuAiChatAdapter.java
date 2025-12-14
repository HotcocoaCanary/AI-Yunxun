package yunxun.ai.canary.backend.mcp.client.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * 智谱 AI聊天适配器
 * <p>
 * 封装智谱AI SDK的调用，提供与 Spring AI ChatClient 类似的接口
 * 注意：此类的Bean通过 McpClientConfig 配置类创建，不使用 @Service 注解
 */
public class ZhipuAiChatAdapter {

    private final ZhipuAiClient client;
    private final String model;

    public ZhipuAiChatAdapter(ZhipuAiClient client, String model) {
        this.client = client;
        this.model = model;
    }

    /**
     * 同步聊天调用
     *
     * @param systemPrompt 系统提示词
     * @param userMessage   用户消息
     * @return 模型回复内容
     */
    public String chat(String systemPrompt, String userMessage) {
        System.out.println("ZhipuAiChatAdapter.chat 开始调用");
        System.out.println("模型: " + model);
        System.out.println("系统提示词长度: " + (systemPrompt != null ? systemPrompt.length() : 0));
        System.out.println("用户消息长度: " + (userMessage != null ? userMessage.length() : 0));
        
        List<ChatMessage> messages = buildMessages(systemPrompt, userMessage);
        System.out.println("构建消息数量: " + messages.size());

        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .temperature(0.6f)
                .maxTokens(2048)
                .build();

        try {
            System.out.println("开始调用智谱AI API...");
            ChatCompletionResponse response = client.chat().createChatCompletion(request);
            System.out.println("智谱AI API 调用完成");
            
            if (response == null) {
                System.err.println("智谱AI响应为null");
                return "";
            }
            
            if (response.getData() != null && 
                response.getData().getChoices() != null && 
                !response.getData().getChoices().isEmpty()) {
                ChatMessage responseMessage = response.getData().getChoices().get(0).getMessage();
                String content = responseMessage != null ? responseMessage.getContent().toString() : "";
                System.out.println("获取到回复内容，长度: " + content.length());
                if (!content.isEmpty()) {
                    System.out.println("回复内容预览: " + content.substring(0, Math.min(200, content.length())));
                }
                return content;
            }
            System.err.println("智谱AI响应中没有choices数据");
            return "";
        } catch (Exception e) {
            System.err.println("智谱AI调用失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("智谱AI调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 响应式聊天调用（返回 Mono）
     *
     * @param systemPrompt 系统提示词
     * @param userMessage   用户消息
     * @return Mono 包装的模型回复内容
     */
    public Mono<String> chatReactive(String systemPrompt, String userMessage) {
        return Mono.fromCallable(() -> chat(systemPrompt, userMessage))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 流式聊天调用
     * 注意：当前使用同步调用模拟流式响应，按词分割返回
     * 如果SDK支持真正的流式调用，可以后续优化
     *
     * @param systemPrompt 系统提示词
     * @param userMessage   用户消息
     * @return 流式响应内容
     */
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        // 使用响应式方法，然后转换为流式
        return chatReactive(systemPrompt, userMessage)
                .flatMapMany(fullResponse -> {
                    if (fullResponse == null || fullResponse.isEmpty()) {
                        return Flux.empty();
                    }
                    // 将完整响应按词分割（中英文混合），模拟流式返回
                    // 使用正则表达式分割：空格、标点符号等
                    String[] words = fullResponse.split("(?<=[\\s，。！？；：、])|(?=[\\s，。！？；：、])");
                    return Flux.fromArray(words)
                            .filter(word -> word != null && !word.trim().isEmpty())
                            .delayElements(java.time.Duration.ofMillis(20));
                })
                .onErrorResume(e -> Flux.error(new RuntimeException("智谱AI流式调用失败: " + e.getMessage(), e)));
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(String systemPrompt, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM.value())
                    .content(systemPrompt)
                    .build());
        }
        
        if (userMessage != null && !userMessage.isBlank()) {
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(userMessage)
                    .build());
        }
        
        return messages;
    }
}

