package com.omnibot.agent;

import com.omnibot.model.ChatMessage.Intent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

/**
 * Rule-based intent detection engine.
 * Analyses user message and returns the best matching Intent.
 * Designed to be replaced/augmented with an ML model in Phase 3.
 */
@Component
public class IntentDetector {

    private static final Map<Intent, List<String>> KEYWORDS = Map.of(
        Intent.FOOD_ORDER, List.of(
            "food", "hungry", "eat", "order", "pizza", "burger", "biryani",
            "zomato", "swiggy", "blinkit", "zepto", "restaurant", "meal",
            "lunch", "dinner", "breakfast", "snack", "deliver food", "cuisine"
        ),
        Intent.TRANSPORT_BOOK, List.of(
            "ride", "cab", "auto", "bike", "taxi", "uber", "ola", "rapido",
            "yulu", "bounce", "drive", "pick me up", "pickup", "drop",
            "travel", "go to", "take me", "book a ride", "transport"
        ),
        Intent.SHOPPING_ORDER, List.of(
            "buy", "shop", "order", "amazon", "flipkart", "meesho", "myntra",
            "nykaa", "product", "purchase", "deliver", "item", "gadget",
            "clothes", "electronics", "mobile", "laptop", "shoes", "dress"
        ),
        Intent.TRACK_ORDER, List.of(
            "track", "where is", "status", "delivery status", "when will",
            "my order", "order id", "shipment", "arrived", "out for delivery"
        ),
        Intent.COMPARE, List.of(
            "compare", "cheaper", "best price", "which is better", "difference",
            "vs", "versus", "faster", "cheaper option", "recommend"
        ),
        Intent.GREETING, List.of(
            "hi", "hello", "hey", "good morning", "good evening", "howdy",
            "what's up", "namaste", "hola", "greetings", "sup"
        ),
        Intent.HELP, List.of(
            "help", "how to", "what can you do", "guide", "assist",
            "support", "confused", "not sure", "explain", "show me"
        )
    );

    public Intent detect(String message) {
        if (message == null || message.isBlank()) return Intent.UNKNOWN;
        String lower = message.toLowerCase().trim();

        Intent best = Intent.UNKNOWN;
        int bestScore = 0;

        for (Map.Entry<Intent, List<String>> entry : KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue()) {
                if (lower.contains(kw)) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                best = entry.getKey();
            }
        }
        return best;
    }
}
