package com.gymmate.ai.application.port;

/**
 * Port abstracting the LLM backend used to generate AI trainer plans. Application
 * services depend only on this interface; concrete adapters (OpenAI today,
 * Claude/others later) plug in without changing caller code — see
 * {@link com.gymmate.ai.infrastructure.OpenAiLlmClientAdapter}.
 *
 * <p>Provider selection is a separate, later decision — this port exists to make that
 * swap a one-adapter change instead of a call-site refactor. Both current call sites
 * ({@code AiPlanService}, {@code AiTrainerService}) do single-shot
 * {@code prompt().user(...).call()}, so the port is deliberately minimal; widen it
 * (streaming, multi-turn, structured output) only when a real caller needs it.
 */
public interface LlmClient {

    /**
     * @param systemPrompt optional system/instruction prompt; may be null or blank
     * @param userPrompt   the user-turn prompt
     * @return the model's text response
     */
    String complete(String systemPrompt, String userPrompt);
}
