package com.omnibot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnibot.agent.BotReplyEngine;
import com.omnibot.agent.MockServiceAdapter;
import com.omnibot.model.ChatDto.ChatResponse;
import com.omnibot.model.ChatDto.ServiceCard;
import com.omnibot.model.ConversationState;
import com.omnibot.model.ConversationState.Step;
import com.omnibot.model.SavedRoute;
import com.omnibot.repository.ConversationStateRepository;
import com.omnibot.repository.SavedRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages multi-turn "slot-filling" conversations.
 *
 * Currently supports the TRANSPORT_BOOK flow:
 *   1. Ask source (pickup location)
 *   2. Ask destination
 *   3. Ask time
 *   4. Ask whether to save the route
 *   5. Ask preferred app
 *   6. Show summary + sorted service cards
 *
 * Designed so FOOD_ORDER / SHOPPING_ORDER flows can be added the same way.
 */
@Service
public class ConversationFlowService {

    private final ConversationStateRepository stateRepo;
    private final SavedRouteRepository savedRouteRepo;
    private final BotReplyEngine botReplyEngine;
    private final MockServiceAdapter mockServiceAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConversationFlowService(ConversationStateRepository stateRepo,
                                   SavedRouteRepository savedRouteRepo,
                                   BotReplyEngine botReplyEngine,
                                   MockServiceAdapter mockServiceAdapter) {
        this.stateRepo = stateRepo;
        this.savedRouteRepo = savedRouteRepo;
        this.botReplyEngine = botReplyEngine;
        this.mockServiceAdapter = mockServiceAdapter;
    }

    /** Returns true if this session has an active multi-step flow in progress. */
    public boolean hasActiveFlow(String sessionId) {
        return stateRepo.findBySessionId(sessionId).isPresent();
    }

    /**
     * Starts a new TRANSPORT_BOOK flow and returns the first question.
     */
    @Transactional
    public ChatResponse startTransportFlow(Long userId, String sessionId) {
        ConversationState state = new ConversationState();
        state.setSessionId(sessionId);
        state.setUserId(userId);
        state.setFlowType(ConversationState.FlowType.TRANSPORT_BOOK);
        state.setCurrentStep(Step.ASK_SOURCE);
        state.setData("{}");
        stateRepo.save(state);

        return buildResponse(sessionId, botReplyEngine.askSource(), "TRANSPORT_BOOK", null);
    }

    /**
     * Processes the next user message as the answer to the current step's question.
     * Advances the flow and returns the next question, or the final result.
     */
    @Transactional
    public ChatResponse continueFlow(Long userId, String sessionId, String userMessage) {
        ConversationState state = stateRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("No active flow for session " + sessionId));

        Map<String, String> data = readData(state);
        String answer = userMessage.trim();

        switch (state.getCurrentStep()) {

            case ASK_SOURCE -> {
                data.put("source", answer);
                state.setCurrentStep(Step.ASK_DESTINATION);
                saveState(state, data);
                return buildResponse(sessionId, botReplyEngine.askDestination(answer), "TRANSPORT_BOOK", null);
            }

            case ASK_DESTINATION -> {
                data.put("destination", answer);
                state.setCurrentStep(Step.ASK_TIME);
                saveState(state, data);
                return buildResponse(sessionId, botReplyEngine.askTime(), "TRANSPORT_BOOK", null);
            }

            case ASK_TIME -> {
                data.put("time", answer.isBlank() ? "now" : answer);
                state.setCurrentStep(Step.ASK_SAVE_ROUTE);
                saveState(state, data);
                String q = botReplyEngine.askSaveRoute(data.get("source"), data.get("destination"));
                return buildResponse(sessionId, q, "TRANSPORT_BOOK", null);
            }

            case ASK_SAVE_ROUTE -> {
                boolean wantsToSave = isYes(answer);
                data.put("saveRoute", String.valueOf(wantsToSave));
                state.setCurrentStep(Step.ASK_PREFERENCE);
                saveState(state, data);

                String prefQuestion = botReplyEngine.askPreference();
                if (wantsToSave) {
                    // We'll save once we know the preferred app (next step)
                    return buildResponse(sessionId,
                        botReplyEngine.routeSavedConfirmation() + "\n\n" + prefQuestion,
                        "TRANSPORT_BOOK", null);
                }
                return buildResponse(sessionId, prefQuestion, "TRANSPORT_BOOK", null);
            }

            case ASK_PREFERENCE -> {
                String preference = normalisePreference(answer);
                data.put("preference", preference);

                // Save route if requested
                if (Boolean.parseBoolean(data.getOrDefault("saveRoute", "false"))) {
                    SavedRoute route = new SavedRoute();
                    route.setUserId(userId);
                    route.setSource(data.get("source"));
                    route.setDestination(data.get("destination"));
                    route.setPreferredApp(preference);
                    savedRouteRepo.save(route);
                }

                // Flow complete — build summary + cards
                String summary = botReplyEngine.bookingSummary(
                        data.get("source"), data.get("destination"),
                        data.get("time"), preference);

                List<ServiceCard> cards = mockServiceAdapter.getTransportCards(preference);

                // Clear the flow state
                stateRepo.deleteBySessionId(sessionId);

                return buildResponse(sessionId, summary, "TRANSPORT_BOOK", cards);
            }

            default -> {
                stateRepo.deleteBySessionId(sessionId);
                return buildResponse(sessionId,
                    "Let's start over — what would you like to do?", "UNKNOWN", null);
            }
        }
    }

    // --------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------

    private boolean isYes(String answer) {
        String a = answer.trim().toLowerCase();
        return a.startsWith("y") || a.contains("sure") || a.contains("ok") || a.contains("yeah");
    }

    private String normalisePreference(String answer) {
        String a = answer.trim().toLowerCase();
        if (a.contains("no preference") || a.contains("any") || a.contains("doesn't matter")
                || a.contains("dont mind") || a.contains("don't mind") || a.isBlank()) {
            return "No preference";
        }
        for (String app : List.of("Uber", "Ola", "Rapido", "Yulu")) {
            if (a.contains(app.toLowerCase())) return app;
        }
        return "No preference";
    }

    private Map<String, String> readData(ConversationState state) {
        try {
            if (state.getData() == null || state.getData().isBlank()) return new HashMap<>();
            return objectMapper.readValue(state.getData(), Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveState(ConversationState state, Map<String, String> data) {
        try {
            state.setData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            state.setData("{}");
        }
        stateRepo.save(state);
    }

    private ChatResponse buildResponse(String sessionId, String reply, String intent, List<ServiceCard> cards) {
        ChatResponse response = new ChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setIntent(intent);
        response.setServices(cards);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
