/**
 * Extension point abstracting the LLM backend, mirroring
 * {@link com.gymmate.access.application.port.AccessDevicePort} — the reference
 * hexagonal port for this codebase. Application services depend only on
 * {@link com.gymmate.ai.application.port.LlmClient}; concrete adapters (OpenAI today,
 * others later) plug in without changing caller code.
 */
@org.springframework.modulith.NamedInterface("application.port")
package com.gymmate.ai.application.port;
