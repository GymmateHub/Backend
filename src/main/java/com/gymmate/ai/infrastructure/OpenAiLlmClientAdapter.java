package com.gymmate.ai.infrastructure;

import com.gymmate.ai.application.port.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Wraps the existing Spring AI {@link ChatClient} (OpenAI, via
 * {@code spring-ai-openai-spring-boot-starter}) behind the {@link LlmClient} port.
 * Zero functional change from the pre-port direct-{@code ChatClient} call sites.
 */
@Component
@RequiredArgsConstructor
public class OpenAiLlmClientAdapter implements LlmClient {

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        var spec = chatClientBuilder.build().prompt();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            spec = spec.system(systemPrompt);
        }
        return spec.user(userPrompt).call().content();
    }
}
