package com.omnibot.service;

import com.omnibot.agent.BotReplyEngine;
import com.omnibot.agent.IntentService;
import com.omnibot.agent.MockServiceAdapter;
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
 * Core chatbot orchestration service.
 *
 * Flow:
 *   1. Receive user message
 *   2. Detect intent (IntentDetector)
 *   3. Generate bot reply (BotReplyEngine)
 *   4. Fetch mock service cards (MockServiceAdapter)
 *   5. Persist both messages to DB
 *   6. Return structured ChatResponse
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

    public ChatService(IntentService intentService,
                       BotReplyEngine botReplyEngine,
                       MockServiceAdapter mockServiceAdapter,
                       VendorAdapterRegistry vendorRegistry,
                       ChatMessageRepository chatRepo,
                       ConversationFlowService flowService) {
        this.intentService = intentService;
        this.botReplyEngine = botReplyEngine;
        this.mockServiceAdapter = mockServiceAdapter;
        this.vendorRegistry = vendorRegistry;
        this.chatRepo = chatRepo;
        this.flowService = flowService;
    }

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        // Generate or reuse session
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        String userMsg = request.getMessage().trim();

        // ------------------------------------------------------------
        // If a multi-step booking flow is already in progress for this
        // session, treat this message as the answer to the current
        // question and let the flow service handle it.
        // ------------------------------------------------------------
        if (flowService.hasActiveFlow(sessionId)) {
            ChatResponse flowResponse = flowService.continueFlow(userId, sessionId, userMsg);
            persistTurn(userId, sessionId, userMsg, flowResponse.getReply(),
                    Intent.valueOf(flowResponse.getIntent()));
            return flowResponse;
        }

        // 1. Detect intent using new NLP service
        java.util.Map<String, Object> parsed = intentService.parseIntent(userMsg);
        String detectedIntentStr = (String) parsed.get("intent");
        
        Intent intent;
        try {
            intent = Intent.valueOf(detectedIntentStr);
        } catch (Exception e) {
            intent = Intent.UNKNOWN;
        }
        
        log.info("User {} | Intent: {} | Message: {}", userId, intent, userMsg);

        // ------------------------------------------------------------
        // TRANSPORT_BOOK and FOOD_ORDER start guided multi-step flows
        // instead of immediately showing cards.
        // ------------------------------------------------------------
        if (intent == Intent.TRANSPORT_BOOK) {
            ChatResponse flowResponse = flowService.startTransportFlow(userId, sessionId);
            persistTurn(userId, sessionId, userMsg, flowResponse.getReply(), intent);
            return flowResponse;
        }
        if (intent == Intent.FOOD_ORDER) {
            ChatResponse flowResponse = flowService.startFoodFlow(userId, sessionId);
            persistTurn(userId, sessionId, userMsg, flowResponse.getReply(), intent);
            return flowResponse;
        }

        // Immediate card responses (no multi-step flow)
        if (intent == Intent.GROCERY_ORDER || intent == Intent.SHOPPING_ORDER
                || intent == Intent.MULTI_INTENT_RIDE_FOOD || intent == Intent.COMPARE) {
            String reply = botReplyEngine.generateReply(intent, userMsg);
            List<ServiceCard> cards = getVendorCards(intent, userMsg);
            persistTurn(userId, sessionId, userMsg, reply, intent);
            ChatResponse response = new ChatResponse();
            response.setSessionId(sessionId);
            response.setReply(reply);
            response.setIntent(intent.name());
            response.setServices(cards);
            response.setTimestamp(LocalDateTime.now());
            return response;
        }

        // 2. Generate reply
        String reply = botReplyEngine.generateReply(intent, userMsg);

        // 3. Get service cards from vendor adapters
        List<ServiceCard> cards = getVendorCards(intent, userMsg);

        // 4-5. Persist messages
        persistTurn(userId, sessionId, userMsg, reply, intent);

        // 6. Build response
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
            default -> resolveCategoryFromMessage(userMessage);
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
                                r.getPrice() + " " + r.getCurrency(),
                                String.valueOf(r.getRating())))
                        .collect(Collectors.toList());
            }
        }

        return mockServiceAdapter.getCards(intent, userMessage);
    }

    private VendorCategory resolveCategoryFromMessage(String msg) {
        if (msg == null) return null;
        String lower = msg.toLowerCase();
        if (containsAny(lower, "ride", "cab", "uber", "ola", "lyft", "bolt", "bike", "auto", "rapido", "yulu"))
            return VendorCategory.TRANSPORT;
        if (containsAny(lower, "food", "eat", "biryani", "pizza", "burger", "restaurant", "zomato", "swiggy"))
            return VendorCategory.FOOD;
        if (containsAny(lower, "buy", "shop", "product", "laptop", "phone", "amazon", "flipkart"))
            return VendorCategory.SHOPPING;
        if (containsAny(lower, "grocery", "vegetable", "milk", "bread"))
            return VendorCategory.GROCERY;
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
