package com.omnibot.service;

import com.omnibot.agent.BotReplyEngine;
import com.omnibot.agent.IntentDetector;
import com.omnibot.agent.MockServiceAdapter;
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

    private final IntentDetector intentDetector;
    private final BotReplyEngine botReplyEngine;
    private final MockServiceAdapter mockServiceAdapter;
    private final ChatMessageRepository chatRepo;
    private final ConversationFlowService flowService;

    public ChatService(IntentDetector intentDetector,
                       BotReplyEngine botReplyEngine,
                       MockServiceAdapter mockServiceAdapter,
                       ChatMessageRepository chatRepo,
                       ConversationFlowService flowService) {
        this.intentDetector = intentDetector;
        this.botReplyEngine = botReplyEngine;
        this.mockServiceAdapter = mockServiceAdapter;
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

        // 1. Detect intent
        Intent intent = intentDetector.detect(userMsg);
        log.info("User {} | Intent: {} | Message: {}", userId, intent, userMsg);

        // ------------------------------------------------------------
        // TRANSPORT_BOOK starts a guided multi-step flow instead of
        // immediately showing cards.
        // ------------------------------------------------------------
        if (intent == Intent.TRANSPORT_BOOK) {
            ChatResponse flowResponse = flowService.startTransportFlow(userId, sessionId);
            persistTurn(userId, sessionId, userMsg, flowResponse.getReply(), intent);
            return flowResponse;
        }

        // 2. Generate reply
        String reply = botReplyEngine.generateReply(intent, userMsg);

        // 3. Get service cards
        List<ServiceCard> cards = mockServiceAdapter.getCards(intent, userMsg);

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
}
