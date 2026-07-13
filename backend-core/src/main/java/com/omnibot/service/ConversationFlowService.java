package com.omnibot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnibot.agent.BotReplyEngine;
import com.omnibot.agent.MockServiceAdapter;
import com.omnibot.model.ChatDto.ChatResponse;
import com.omnibot.model.ChatDto.ServiceCard;
import com.omnibot.model.ConversationState;
import com.omnibot.model.ConversationState.FlowType;
import com.omnibot.model.ConversationState.Step;
import com.omnibot.model.SavedAddress;
import com.omnibot.model.SavedRoute;
import com.omnibot.repository.ConversationStateRepository;
import com.omnibot.repository.SavedAddressRepository;
import com.omnibot.repository.SavedRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages multi-turn "slot-filling" conversations.
 *
 * Supports two guided flows:
 *
 *  TRANSPORT_BOOK:
 *   1. Ask source (pickup location)
 *   2. Ask destination
 *   3. Ask time
 *   4. Ask whether to save the route
 *   5. Ask preferred app
 *   6. Show summary + sorted service cards
 *
 *  FOOD_ORDER:
 *   1. Ask cuisine / craving
 *   2. Ask specific items
 *   3. Ask delivery address
 *   4. Ask delivery time
 *   5. Ask whether to save the address
 *   6. Ask preferred app
 *   7. Show summary + sorted service cards
 *
 * Both flows share the same ConversationState + Step machinery, so a
 * third flow (e.g. SHOPPING_ORDER) can be added the same way later.
 */
@Service
public class ConversationFlowService {

    private final ConversationStateRepository stateRepo;
    private final SavedRouteRepository savedRouteRepo;
    private final SavedAddressRepository savedAddressRepo;
    private final BotReplyEngine botReplyEngine;
    private final MockServiceAdapter mockServiceAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConversationFlowService(ConversationStateRepository stateRepo,
                                   SavedRouteRepository savedRouteRepo,
                                   SavedAddressRepository savedAddressRepo,
                                   BotReplyEngine botReplyEngine,
                                   MockServiceAdapter mockServiceAdapter) {
        this.stateRepo = stateRepo;
        this.savedRouteRepo = savedRouteRepo;
        this.savedAddressRepo = savedAddressRepo;
        this.botReplyEngine = botReplyEngine;
        this.mockServiceAdapter = mockServiceAdapter;
    }

    /** Returns true if this session has an active multi-step flow in progress. */
    public boolean hasActiveFlow(String sessionId) {
        return stateRepo.findBySessionId(sessionId).isPresent();
    }

    // ================================================================
    // START FLOWS
    // ================================================================

    @Transactional
    public ChatResponse startTransportFlow(Long userId, String sessionId) {
        ConversationState state = newState(userId, sessionId, FlowType.TRANSPORT_BOOK, Step.ASK_SOURCE);
        stateRepo.save(state);
        return buildResponse(sessionId, botReplyEngine.askSource(), "TRANSPORT_BOOK", null);
    }

    @Transactional
    public ChatResponse startFoodFlow(Long userId, String sessionId) {
        ConversationState state = newState(userId, sessionId, FlowType.FOOD_ORDER, Step.ASK_CUISINE);
        stateRepo.save(state);
        return buildResponse(sessionId, botReplyEngine.askCuisine(), "FOOD_ORDER", null);
    }

    private ConversationState newState(Long userId, String sessionId, FlowType type, Step firstStep) {
        ConversationState state = new ConversationState();
        state.setSessionId(sessionId);
        state.setUserId(userId);
        state.setFlowType(type);
        state.setCurrentStep(firstStep);
        state.setData("{}");
        return state;
    }

    // ================================================================
    // CONTINUE AN ACTIVE FLOW
    // ================================================================

    @Transactional
    public ChatResponse continueFlow(Long userId, String sessionId, String userMessage) {
        ConversationState state = stateRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("No active flow for session " + sessionId));

        return switch (state.getFlowType()) {
            case TRANSPORT_BOOK -> continueTransportFlow(userId, sessionId, userMessage, state);
            case FOOD_ORDER     -> continueFoodFlow(userId, sessionId, userMessage, state);
            case SHOPPING_ORDER -> {
                stateRepo.deleteBySessionId(sessionId);
                yield buildResponse(sessionId, "Shopping flow coming soon! What else can I help with?", "UNKNOWN", null);
            }
        };
    }

    // ----------------------------------------------------------------
    // TRANSPORT_BOOK flow
    // ----------------------------------------------------------------
    private ChatResponse continueTransportFlow(Long userId, String sessionId, String userMessage, ConversationState state) {
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
                    return buildResponse(sessionId,
                        botReplyEngine.routeSavedConfirmation() + "\n\n" + prefQuestion,
                        "TRANSPORT_BOOK", null);
                }
                return buildResponse(sessionId, prefQuestion, "TRANSPORT_BOOK", null);
            }

            case ASK_PREFERENCE -> {
                String preference = normaliseAppPreference(answer, MockServiceAdapter.TRANSPORT_VENDORS);
                data.put("preference", preference);

                if (Boolean.parseBoolean(data.getOrDefault("saveRoute", "false"))) {
                    SavedRoute route = new SavedRoute();
                    route.setUserId(userId);
                    route.setSource(data.get("source"));
                    route.setDestination(data.get("destination"));
                    route.setPreferredApp(preference);
                    savedRouteRepo.save(route);
                }

                String summary = botReplyEngine.bookingSummary(
                        data.get("source"), data.get("destination"),
                        data.get("time"), preference);

                List<ServiceCard> cards = mockServiceAdapter.getTransportCards(preference);
                state.setCurrentStep(Step.AWAIT_SELECTION);
                saveState(state, data);
                return buildResponse(sessionId, summary, "TRANSPORT_BOOK", cards);
            }

            case AWAIT_SELECTION -> {
                String vendor = extractSelectedVendor(answer, MockServiceAdapter.TRANSPORT_VENDORS);
                if (vendor != null) {
                    String confirm = "✅ Booking confirmed with **" + vendor + "**!\n\n"
                            + "📍 From: " + data.get("source") + "\n"
                            + "🎯 To: " + data.get("destination") + "\n"
                            + "⏰ Time: " + data.get("time") + "\n\n"
                            + "Your driver is on the way. You'll receive a confirmation shortly.\n"
                            + "Is there anything else I can help with?";
                    stateRepo.deleteBySessionId(sessionId);
                    return buildResponse(sessionId, confirm, "TRANSPORT_BOOK", null);
                }
                stateRepo.deleteBySessionId(sessionId);
                return buildResponse(sessionId, "Let's start over — what would you like to do?", "UNKNOWN", null);
            }

            default -> {
                stateRepo.deleteBySessionId(sessionId);
                return buildResponse(sessionId, "Let's start over — what would you like to do?", "UNKNOWN", null);
            }
        }
    }

    // ----------------------------------------------------------------
    // FOOD_ORDER flow
    // ----------------------------------------------------------------
    private ChatResponse continueFoodFlow(Long userId, String sessionId, String userMessage, ConversationState state) {
        Map<String, String> data = readData(state);
        String answer = userMessage.trim();

        switch (state.getCurrentStep()) {

            case ASK_CUISINE -> {
                data.put("cuisine", answer);
                state.setCurrentStep(Step.ASK_ITEMS);
                saveState(state, data);
                return buildResponse(sessionId, botReplyEngine.askItems(answer), "FOOD_ORDER", null);
            }

            case ASK_ITEMS -> {
                data.put("items", isSkip(answer) ? "" : answer);
                state.setCurrentStep(Step.ASK_ADDRESS);
                saveState(state, data);
                return buildResponse(sessionId, botReplyEngine.askAddress(), "FOOD_ORDER", null);
            }

            case ASK_ADDRESS -> {
                data.put("address", answer);
                state.setCurrentStep(Step.ASK_DELIVERY_TIME);
                saveState(state, data);
                return buildResponse(sessionId, botReplyEngine.askDeliveryTime(), "FOOD_ORDER", null);
            }

            case ASK_DELIVERY_TIME -> {
                data.put("time", answer.isBlank() ? "now" : answer);
                state.setCurrentStep(Step.ASK_SAVE_ADDRESS);
                saveState(state, data);
                String q = botReplyEngine.askSaveAddress(data.get("address"));
                return buildResponse(sessionId, q, "FOOD_ORDER", null);
            }

            case ASK_SAVE_ADDRESS -> {
                boolean wantsToSave = isYes(answer);
                data.put("saveAddress", String.valueOf(wantsToSave));
                state.setCurrentStep(Step.ASK_FOOD_PREFERENCE);
                saveState(state, data);

                String prefQuestion = botReplyEngine.askFoodPreference();
                if (wantsToSave) {
                    return buildResponse(sessionId,
                        botReplyEngine.addressSavedConfirmation() + "\n\n" + prefQuestion,
                        "FOOD_ORDER", null);
                }
                return buildResponse(sessionId, prefQuestion, "FOOD_ORDER", null);
            }

            case ASK_FOOD_PREFERENCE -> {
                String preference = normaliseAppPreference(answer, MockServiceAdapter.FOOD_VENDORS);
                data.put("preference", preference);

                if (Boolean.parseBoolean(data.getOrDefault("saveAddress", "false"))) {
                    SavedAddress saved = new SavedAddress();
                    saved.setUserId(userId);
                    saved.setAddress(data.get("address"));
                    saved.setPreferredApp(preference);
                    savedAddressRepo.save(saved);
                }

                String summary = botReplyEngine.foodOrderSummary(
                        data.get("cuisine"), data.get("items"),
                        data.get("address"), data.get("time"), preference);

                List<ServiceCard> cards = mockServiceAdapter.getFoodCards(preference);
                state.setCurrentStep(Step.AWAIT_FOOD_SELECTION);
                saveState(state, data);
                return buildResponse(sessionId, summary, "FOOD_ORDER", cards);
            }

            case AWAIT_FOOD_SELECTION -> {
                String vendor = extractSelectedVendor(answer, MockServiceAdapter.FOOD_VENDORS);
                if (vendor != null) {
                    String confirm = "✅ Order placed with **" + vendor + "**!\n\n"
                            + "🍽️ Cuisine: " + data.get("cuisine") + "\n"
                            + "📍 Deliver to: " + data.get("address") + "\n"
                            + "⏰ Time: " + data.get("time") + "\n\n"
                            + "Your order is being prepared. You'll get updates shortly.\n"
                            + "Is there anything else I can help with?";
                    stateRepo.deleteBySessionId(sessionId);
                    return buildResponse(sessionId, confirm, "FOOD_ORDER", null);
                }
                stateRepo.deleteBySessionId(sessionId);
                return buildResponse(sessionId, "Let's start over — what would you like to do?", "UNKNOWN", null);
            }

            default -> {
                stateRepo.deleteBySessionId(sessionId);
                return buildResponse(sessionId, "Let's start over — what would you like to do?", "UNKNOWN", null);
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

    private boolean isSkip(String answer) {
        String a = answer.trim().toLowerCase();
        return a.isBlank() || a.contains("surprise") || a.contains("anything") || a.contains("popular");
    }

    private String extractSelectedVendor(String answer, List<String> validApps) {
        String a = answer.trim().toLowerCase();
        if (a.contains("no") || a.isBlank()) return null;
        for (String app : validApps) {
            if (a.contains(app.toLowerCase())) return app;
        }
        if (a.contains("uber") && a.contains("eat")) return "UberEats";
        if (a.contains("uber")) return "Uber";
        if (a.contains("bolt")) return "Bolt";
        if (a.contains("ola")) return "Ola";
        if (a.contains("lyft")) return "Lyft";
        if (a.contains("rapido")) return "Rapido";
        if (a.contains("yulu")) return "Yulu";
        if (a.contains("zomato")) return "Zomato";
        if (a.contains("swiggy")) return "Swiggy";
        if (a.contains("doordash")) return "DoorDash";
        if (a.contains("select") || a.contains("want") || a.contains("use") || a.contains("book")) {
            return "No preference";
        }
        return null;
    }

    private String normaliseAppPreference(String answer, List<String> validApps) {
        String a = answer.trim().toLowerCase();
        if (a.contains("no preference") || a.contains("any") || a.contains("doesn't matter")
                || a.contains("dont mind") || a.contains("don't mind") || a.isBlank()) {
            return "No preference";
        }
        for (String app : validApps) {
            if (a.contains(app.toLowerCase())) return app;
        }
        // Fuzzy match: "uber eats" -> UberEats, "d mart" -> D-Mart
        if (a.contains("uber") && a.contains("eat")) return "UberEats";
        if (a.contains("door") && a.contains("dash")) return "DoorDash";
        if (a.contains("d") && a.contains("mart")) return "D-Mart";
        if (a.contains("jio")) return "JioMart";
        if (a.contains("big") && a.contains("basket")) return "BigBasket";
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
