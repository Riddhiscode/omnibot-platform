package com.omnibot.service;

import com.omnibot.agent.BotReplyEngine;
import com.omnibot.agent.IntentService;
import com.omnibot.agent.MockServiceAdapter;
import com.omnibot.agent.SpringAiVendorTools;
import com.omnibot.agent.tools.WriteOperations;
import com.omnibot.adapter.VendorAdapterRegistry;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.VendorSearchRequest;
import com.omnibot.adapter.dto.VendorSearchResult;
import com.omnibot.model.ChatDto.*;
import com.omnibot.model.ChatMessage;
import com.omnibot.model.ChatMessage.Intent;
import com.omnibot.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core chatbot orchestration service incorporating Spring AI Tools.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final IntentService intentService;
    private final BotReplyEngine botReplyEngine;
    private final MockServiceAdapter mockServiceAdapter;
    private final VendorAdapterRegistry vendorRegistry;
    private final ChatMessageRepository chatRepo;
    private final ConversationFlowService flowService;
    private final SpringAiVendorTools springAiVendorTools;
    private final WriteOperations writeOperations;

    public ChatService(IntentService intentService,
                       BotReplyEngine botReplyEngine,
                       MockServiceAdapter mockServiceAdapter,
                       VendorAdapterRegistry vendorRegistry,
                       ChatMessageRepository chatRepo,
                       ConversationFlowService flowService,
                       SpringAiVendorTools springAiVendorTools,
                       WriteOperations writeOperations) {
        this.intentService = intentService;
        this.botReplyEngine = botReplyEngine;
        this.mockServiceAdapter = mockServiceAdapter;
        this.vendorRegistry = vendorRegistry;
        this.chatRepo = chatRepo;
        this.flowService = flowService;
        this.springAiVendorTools = springAiVendorTools;
        this.writeOperations = writeOperations;
    }

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        String userMsg = request.getMessage().trim();

        if (flowService.hasActiveFlow(sessionId)) {
            ChatResponse flowResponse = flowService.continueFlow(userId, sessionId, userMsg);
            persistTurn(userId, sessionId, userMsg, flowResponse.getReply(),
                    Intent.valueOf(flowResponse.getIntent()));
            return flowResponse;
        }

        // 1. Detect intent
        java.util.Map<String, Object> parsed = intentService.parseIntent(userMsg);
        String detectedIntentStr = (String) parsed.get("intent");
        
        Intent intent;
        try {
            intent = Intent.valueOf(detectedIntentStr);
        } catch (Exception e) {
            intent = Intent.UNKNOWN;
        }
        
        log.info("User {} | Session {} | Intent: {} | Message: {}", userId, sessionId, intent, userMsg);

        // Fast-path: Provide instant, time-saving 1-step response without tedious back-and-forth slot filling questions
        String reply = botReplyEngine.generateReply(intent, userMsg);
        List<ServiceCard> cards = getVendorCards(intent, userMsg);

        // If intent is FOOD_ORDER or TRANSPORT_BOOK, ensure cards are immediately populated from vendor registry
        if (cards == null || cards.isEmpty()) {
            VendorCategory category = switch (intent) {
                case FOOD_ORDER -> VendorCategory.FOOD;
                case TRANSPORT_BOOK -> VendorCategory.TRANSPORT;
                case GROCERY_ORDER -> VendorCategory.GROCERY;
                case SHOPPING_ORDER -> VendorCategory.SHOPPING;
                default -> null;
            };

            if (category != null) {
                VendorSearchRequest searchReq = new VendorSearchRequest();
                searchReq.setQuery(userMsg);
                searchReq.setLocation("Current Location");
                List<VendorSearchResult> searchResults = vendorRegistry.searchAll(category, searchReq);

                cards = searchResults.stream().map(res -> new ServiceCard(
                        res.getVendorName(),
                        res.getVendorName().equalsIgnoreCase("Swiggy") ? "🍊" : "⚡",
                        res.getVendorName().equalsIgnoreCase("Swiggy") ? "swiggy://explore?query=" + userMsg : "zomato://search?q=" + userMsg,
                        res.getEtaLabel() != null ? res.getEtaLabel() : "15-25 mins",
                        "₹" + (res.getPrice() != null ? res.getPrice().intValue() : 199),
                        String.valueOf(res.getRating() > 0 ? res.getRating() : 4.5)
                )).collect(Collectors.toList());
            }
        }

        persistTurn(userId, sessionId, userMsg, reply, intent);

        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setIntent(intent.name());
        response.setServices(cards);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    private void persistTurn(Long userId, String sessionId, String userMsg, String botReply, Intent intent) {
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setSessionId(sessionId);
        userMessage.setRole(ChatMessage.Role.USER);
        userMessage.setContent(userMsg);
        userMessage.setIntent(intent);
        chatRepo.save(userMessage);

        ChatMessage botMessage = new ChatMessage();
        botMessage.setUserId(userId);
        botMessage.setSessionId(sessionId);
        botMessage.setRole(ChatMessage.Role.BOT);
        botMessage.setContent(botReply);
        botMessage.setIntent(intent);
        chatRepo.save(botMessage);
    }

    public List<ChatHistoryItem> getHistory(Long userId, String sessionId) {
        return chatRepo.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .filter(m -> m.getUserId().equals(userId))
                .map(m -> new ChatHistoryItem(
                        m.getRole().name(),
                        m.getContent(),
                        m.getIntent() != null ? m.getIntent().name() : null,
                        m.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private List<ServiceCard> getVendorCards(Intent intent, String userMessage) {
        VendorCategory category = switch (intent) {
            case FOOD_ORDER -> VendorCategory.FOOD;
            case SHOPPING_ORDER -> VendorCategory.SHOPPING;
            case TRANSPORT_BOOK -> VendorCategory.TRANSPORT;
            case GROCERY_ORDER -> VendorCategory.GROCERY;
            default -> null;
        };

        if (category != null) {
            VendorSearchRequest searchReq = new VendorSearchRequest(userMessage, userMessage);
            List<VendorSearchResult> results = vendorRegistry.searchAll(category, searchReq);
            if (!results.isEmpty()) {
                return results.stream()
                        .map(r -> new ServiceCard(
                                r.getVendorName(),
                                "vendor",
                                category == VendorCategory.FOOD ? "ORDER_FOOD"
                                        : category == VendorCategory.TRANSPORT ? "BOOK_RIDE"
                                        : "BUY_NOW",
                                r.getEtaLabel() != null ? r.getEtaLabel() : r.getEtaMinutes() + " mins",
                                "₹" + (r.getPrice() != null ? r.getPrice().intValue() : 150),
                                String.valueOf(r.getRating())))
                        .collect(Collectors.toList());
            }
        }

        return mockServiceAdapter.getCards(intent, userMessage);
    }
}
