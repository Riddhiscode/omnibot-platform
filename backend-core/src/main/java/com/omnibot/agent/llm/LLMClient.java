package com.omnibot.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LLM client targeting Anthropic's Claude API.
 *
 * Security measures:
 * 1. System prompt is loaded from classpath at startup — never from user input.
 * 2. User messages are sanitised before being sent to the API.
 * 3. Tool schemas are built server-side and injected by this class — not by callers.
 * 4. API key is read from environment variable only — never from the DB or user input.
 * 5. A mock mode activates automatically when no API key is configured, so the
 *    rest of the system keeps working without a key.
 */
@Component
public class LLMClient {

    private static final Logger log = LoggerFactory.getLogger(LLMClient.class);
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-6";
    private static final int MAX_TOKENS = 1024;

    @Value("${omnibot.llm.api-key:}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String systemPrompt;

    public LLMClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl(ANTHROPIC_API_URL)
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("content-type", "application/json")
            .build();
    }

    @PostConstruct
    public void loadSystemPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/omnicore_system.txt");
            this.systemPrompt = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("OmniCore system prompt loaded ({} chars)", systemPrompt.length());
        } catch (IOException e) {
            log.error("Failed to load system prompt — using fallback", e);
            this.systemPrompt = "You are OmniBot, a helpful service assistant for food, transport, and shopping.";
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Send a conversation turn to the LLM and get a response.
     *
     * @param conversationHistory full prior turns as [{role, content}] pairs
     * @param userMessage the latest user message (will be sanitised)
     * @return LLMResponse containing either a text reply or tool calls
     */
    public CompletableFuture<LLMResponse> chat(
            List<Map<String, Object>> conversationHistory,
            String userMessage) {

        if (!isConfigured()) {
            log.debug("No API key — using mock LLM response");
            return CompletableFuture.completedFuture(mockResponse(userMessage));
        }

        String sanitisedMessage = sanitiseUserInput(userMessage);

        try {
            ObjectNode requestBody = buildRequestBody(conversationHistory, sanitisedMessage);
            String requestJson = objectMapper.writeValueAsString(requestBody);

            return webClient.post()
                .header("x-api-key", apiKey)
                .bodyValue(requestJson)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("LLM API error: " + body))))
                .bodyToMono(String.class)
                .map(this::parseResponse)
                .toFuture()
                .exceptionally(ex -> {
                    log.error("LLM API call failed: {}", ex.getMessage());
                    return errorResponse("I'm having trouble connecting right now. Please try again in a moment.");
                });

        } catch (Exception e) {
            log.error("Failed to build LLM request: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                errorResponse("Something went wrong. Please try again."));
        }
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private ObjectNode buildRequestBody(List<Map<String, Object>> history, String userMessage)
            throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", MODEL);
        body.put("max_tokens", MAX_TOKENS);
        body.put("system", systemPrompt);

        // Build messages array: history + new user message
        ArrayNode messages = objectMapper.createArrayNode();
        for (Map<String, Object> turn : history) {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("role", (String) turn.get("role"));
            msg.put("content", (String) turn.get("content"));
            messages.add(msg);
        }
        ObjectNode newMessage = objectMapper.createObjectNode();
        newMessage.put("role", "user");
        newMessage.put("content", userMessage);
        messages.add(newMessage);
        body.set("messages", messages);

        // Inject tool schemas server-side
        body.set("tools", objectMapper.valueToTree(OmniCoreTools.getAllToolSchemas()));

        return body;
    }

    private LLMResponse parseResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode content = root.path("content");

            LLMResponse response = new LLMResponse();

            for (JsonNode block : content) {
                String type = block.path("type").asText();

                if ("text".equals(type)) {
                    response.setTextReply(block.path("text").asText());

                } else if ("tool_use".equals(type)) {
                    LLMResponse.ToolCall toolCall = new LLMResponse.ToolCall();
                    toolCall.setId(block.path("id").asText());
                    toolCall.setName(block.path("name").asText());
                    toolCall.setInput(block.path("input"));
                    response.addToolCall(toolCall);
                }
            }

            response.setStopReason(root.path("stop_reason").asText());
            return response;

        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            return errorResponse("I couldn't process that response. Please try again.");
        }
    }

    /**
     * Sanitise user input to prevent prompt injection attacks.
     * Removes patterns that attempt to override system instructions.
     */
    private String sanitiseUserInput(String input) {
        if (input == null) return "";

        // Truncate to prevent token flooding
        String truncated = input.length() > 2000 ? input.substring(0, 2000) + "..." : input;

        // Detect and neutralise common injection patterns
        String lower = truncated.toLowerCase();
        if (lower.contains("ignore previous instructions") ||
            lower.contains("disregard your") ||
            lower.contains("new system prompt") ||
            lower.contains("you are now") ||
            lower.contains("pretend you are") ||
            lower.contains("act as if") ||
            lower.contains("forget all previous") ||
            lower.contains("override your")) {

            log.warn("Potential prompt injection detected in user input — sanitising");
            return "[Message filtered: contained instruction override attempt] "
                + "I'd like to use OmniBot for: " + truncated.substring(0, Math.min(100, truncated.length()));
        }

        return truncated;
    }

    /**
     * Mock response when no API key is configured.
     * Simulates LLM intent detection for the four supported categories.
     */
    private LLMResponse mockResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        LLMResponse response = new LLMResponse();

        if (containsAny(lower, "food", "hungry", "eat", "pizza", "biryani", "zomato", "swiggy")) {
            response.setTextReply("I'll search for food options for you! Could you tell me your delivery address?");
        } else if (containsAny(lower, "cab", "ride", "uber", "ola", "rapido", "transport", "taxi")) {
            response.setTextReply("I'll find you a ride! Where are you picking up from, and where are you headed?");
        } else if (containsAny(lower, "buy", "shop", "amazon", "flipkart", "order", "product")) {
            response.setTextReply("I'll search for that across Amazon, Flipkart, and more. What are you looking for?");
        } else if (containsAny(lower, "hello", "hi", "hey", "help")) {
            response.setTextReply("Hello! I'm OmniBot. I can help you with food delivery, cab booking, grocery, and online shopping — all in one place. What would you like today?");
        } else {
            response.setTextReply("I can help you with food, transport, grocery, or shopping. Which service do you need?");
        }

        return response;
    }

    private LLMResponse errorResponse(String message) {
        LLMResponse response = new LLMResponse();
        response.setTextReply(message);
        response.setError(true);
        return response;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }
}
