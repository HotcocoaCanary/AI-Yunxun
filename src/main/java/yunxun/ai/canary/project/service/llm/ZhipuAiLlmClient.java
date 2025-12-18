package yunxun.ai.canary.project.service.llm;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.Choice;
import ai.z.openapi.service.model.Delta;
import ai.z.openapi.service.model.ModelData;
import io.reactivex.rxjava3.core.Flowable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ZhipuAiLlmClient implements LlmClient {

    private final ZhipuAiClient client;
    private final String model;

    public ZhipuAiLlmClient(ZhipuAiClient client, String model) {
        this.client = Objects.requireNonNull(client, "client");
        this.model = Objects.requireNonNull(model, "model");
    }

    @Override
    public Mono<String> complete(List<LlmMessage> messages) {
        return Mono.fromCallable(() -> {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .messages(toZhipuMessages(messages))
                    .stream(false)
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(params);
            if (response == null) {
                throw new IllegalStateException("Zhipu chat completion returned null response");
            }
            if (!response.isSuccess()) {
                String msg = response.getMsg() != null ? response.getMsg() : "Zhipu chat completion failed";
                throw new IllegalStateException(msg);
            }

            ModelData data = response.getData();
            String text = extractText(data);
            if (text == null || text.isBlank()) {
                throw new IllegalStateException("Zhipu chat completion returned empty content");
            }
            return text;
        });
    }

    @Override
    public Flux<String> stream(List<LlmMessage> messages) {
        return Flux.defer(() -> {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(model)
                    .messages(toZhipuMessages(messages))
                    .stream(true)
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(params);
            if (response == null) {
                return Flux.error(new IllegalStateException("Zhipu chat completion stream returned null response"));
            }
            if (!response.isSuccess()) {
                String msg = response.getMsg() != null ? response.getMsg() : "Zhipu chat completion stream failed";
                return Flux.error(new IllegalStateException(msg));
            }

            Flowable<ModelData> flowable = response.getFlowable();
            if (flowable == null) {
                return Flux.error(new IllegalStateException("Zhipu chat completion stream missing flowable"));
            }

            return Flux.create(sink -> flowable.subscribe(
                    data -> {
                        String delta = extractDelta(data);
                        if (delta != null && !delta.isEmpty()) {
                            sink.next(delta);
                        }
                    },
                    sink::error,
                    sink::complete
            ));
        });
    }

    private static List<ChatMessage> toZhipuMessages(List<LlmMessage> messages) {
        List<ChatMessage> result = new ArrayList<>();
        if (messages == null) {
            return result;
        }
        for (LlmMessage message : messages) {
            if (message == null) {
                continue;
            }
            String role = message.role() == null ? "user" : message.role();
            String content = message.content() == null ? "" : message.content();
            result.add(new ChatMessage(role, content));
        }
        return result;
    }

    private static String extractText(ModelData data) {
        if (data == null) {
            return null;
        }
        if (data.getText() != null && !data.getText().isBlank()) {
            return data.getText();
        }
        List<Choice> choices = data.getChoices();
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        ChatMessage msg = choices.get(0).getMessage();
        if (msg == null) {
            return null;
        }
        Object content = msg.getContent();
        return content == null ? null : content.toString();
    }

    private static String extractDelta(ModelData data) {
        if (data == null) {
            return null;
        }
        if (data.getDelta() != null && !data.getDelta().isEmpty()) {
            return data.getDelta();
        }
        List<Choice> choices = data.getChoices();
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        Delta delta = choices.get(0).getDelta();
        return delta == null ? null : delta.getContent();
    }
}
