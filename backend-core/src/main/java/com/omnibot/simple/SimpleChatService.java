package com.omnibot.simple;

import com.omnibot.simple.SimpleChatDto.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Simple self-contained chat service.
 *
 * Rules:
 *  - NO external API calls, NO complex dependencies.
 *  - Mock vendor data is hardcoded in static arrays.
 *  - Multi-turn memory is a plain HashMap<sessionId, SessionState>.
 *  - Intent detection is simple keyword matching.
 */
@Service
public class SimpleChatService {

    // ── In-memory session state (cleared on restart) ─────────────────
    private static class SessionState {
        String intent = null;   // FOOD | CABS | SHOPPING
        String step   = "start"; // start → got_location → done
        String location = null;
    }

    private final Map<String, SessionState> sessions = new HashMap<>();

    // ── Hardcoded mock vendor data ────────────────────────────────────
    private static final List<Option> FOOD_OPTIONS = List.of(
        new Option("Zomato",   "₹35 delivery", "25 mins", false, false),
        new Option("Swiggy",   "₹29 delivery", "30 mins", true,  false),
        new Option("UberEats", "₹45 delivery", "20 mins", false, true),
        new Option("DoorDash", "₹39 delivery", "28 mins", false, false)
    );

    private static final List<Option> CABS_OPTIONS = List.of(
        new Option("Uber",   "₹120", "4 mins away", false, true),
        new Option("Ola",    "₹105", "6 mins away", true,  false),
        new Option("Rapido", "₹65",  "3 mins away", false, false),
        new Option("Lyft",   "₹130", "8 mins away", false, false),
        new Option("Bolt",   "₹98",  "5 mins away", false, false)
    );

    private static final List<Option> SHOPPING_OPTIONS = List.of(
        new Option("Amazon",   "₹1,499", "Tomorrow",  false, true),
        new Option("Flipkart", "₹1,349", "2 days",    true,  false),
        new Option("Meesho",   "₹1,199", "4 days",    false, false),
        new Option("Myntra",   "₹1,599", "2 days",    false, false),
        new Option("eBay",     "₹1,249", "5 days",    false, false)
    );

    // ── Entry point ───────────────────────────────────────────────────
    public Response chat(String sessionId, String message) {
        SessionState state = sessions.computeIfAbsent(sessionId, id -> new SessionState());
        String msg = message.trim().toLowerCase();

        // If a flow is in progress, handle the follow-up answer
        if (!"start".equals(state.step)) {
            return continueFlow(sessionId, state, message);
        }

        // Detect intent from fresh message
        String intent = detectIntent(msg);
        state.intent = intent;

        return switch (intent) {
            case "FOOD"     -> askLocation(sessionId, state, "food", "🍕 Sure! Where should food be delivered to?");
            case "CABS"     -> askLocation(sessionId, state, "cabs", "🚗 Got it! Where are you picking up the ride from?");
            case "SHOPPING" -> askLocation(sessionId, state, "shopping", "🛒 Happy to help! What city/pincode should I search in?");
            default         -> unknown(sessionId, state, msg);
        };
    }

    // ── Continue an in-progress flow ──────────────────────────────────
    private Response continueFlow(String sessionId, SessionState state, String message) {
        // Step: waiting for location
        if ("waiting_location".equals(state.step)) {
            state.location = message.trim();
            state.step     = "done";

            List<Option> options = switch (state.intent) {
                case "FOOD"     -> FOOD_OPTIONS;
                case "CABS"     -> CABS_OPTIONS;
                case "SHOPPING" -> SHOPPING_OPTIONS;
                default         -> List.of();
            };

            String reply = switch (state.intent) {
                case "FOOD"     -> "Here are the best food delivery options near **" + state.location + "**:";
                case "CABS"     -> "Cabs available from **" + state.location + "**:";
                case "SHOPPING" -> "Best prices for delivery to **" + state.location + "**:";
                default         -> "Here are your options:";
            };

            // Reset flow so user can start a new one
            state.step   = "start";
            state.intent = null;

            return new Response(sessionId, reply, state.intent, "done", options);
        }

        // Unexpected state — reset
        state.step   = "start";
        state.intent = null;
        return new Response(sessionId,
            "Sorry, I got confused! Ask me to order food, book a cab, or find a product.",
            "UNKNOWN", "start", List.of());
    }

    // ── Intent detection — simple keyword match ───────────────────────
    private String detectIntent(String msg) {
        if (containsAny(msg, "food","eat","hungry","pizza","biryani","burger","order","swiggy","zomato","ubereats","doordash"))
            return "FOOD";
        if (containsAny(msg, "cab","ride","uber","ola","taxi","auto","bike","rapido","bolt","lyft","transport","pick up","drop"))
            return "CABS";
        if (containsAny(msg, "buy","shop","product","laptop","phone","mobile","amazon","flipkart","myntra","meesho","ebay","purchase"))
            return "SHOPPING";
        return "UNKNOWN";
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private Response askLocation(String sessionId, SessionState state, String intentLabel, String question) {
        state.step   = "waiting_location";
        state.intent = intentLabel.toUpperCase();
        return new Response(sessionId, question, state.intent, "waiting_location", List.of());
    }

    private Response unknown(String sessionId, SessionState state, String msg) {
        return new Response(sessionId,
            "I can help you order food 🍕, book a cab 🚗, or find the best shopping deals 🛒. What would you like?",
            "UNKNOWN", "start", List.of());
    }

    private boolean containsAny(String text, String... words) {
        for (String w : words) if (text.contains(w)) return true;
        return false;
    }
}
