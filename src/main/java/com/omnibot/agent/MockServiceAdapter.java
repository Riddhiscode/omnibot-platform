package com.omnibot.agent;

import com.omnibot.model.ChatDto.ServiceCard;
import com.omnibot.model.ChatMessage.Intent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Mock API adapters simulating real service responses.
 * Replace each method body with real API calls once partnerships are established.
 *
 * Services simulated:
 *   Food    : Zomato, Swiggy, Blinkit, Zepto
 *   Transport: Uber, Ola, Rapido, Yulu
 *   Shopping : Amazon, Flipkart, Meesho, Myntra
 */
@Component
public class MockServiceAdapter {

    private final Random rng = new Random();

    public List<ServiceCard> getCards(Intent intent, String userMessage) {
        return switch (intent) {
            case FOOD_ORDER      -> foodCards(userMessage);
            case TRANSPORT_BOOK  -> transportCards(userMessage);
            case SHOPPING_ORDER  -> shoppingCards(userMessage);
            default              -> List.of();
        };
    }

    /**
     * Returns transport cards with the preferred app (if any) listed first.
     */
    public List<ServiceCard> getTransportCards(String preferredApp) {
        List<ServiceCard> cards = transportCards("");
        if (preferredApp == null || preferredApp.isBlank()) return cards;

        String pref = preferredApp.trim().toLowerCase();
        return cards.stream()
                .sorted((a, b) -> {
                    boolean aMatch = a.getName().toLowerCase().equals(pref);
                    boolean bMatch = b.getName().toLowerCase().equals(pref);
                    if (aMatch && !bMatch) return -1;
                    if (!aMatch && bMatch) return 1;
                    return 0;
                })
                .toList();
    }

    // ----------------------------------------------------------------
    // FOOD DELIVERY
    // ----------------------------------------------------------------
    private List<ServiceCard> foodCards(String msg) {
        int eta1 = 20 + rng.nextInt(15);
        int eta2 = 25 + rng.nextInt(20);
        int eta3 = 10 + rng.nextInt(10);
        int eta4 = 12 + rng.nextInt(8);

        return List.of(
            new ServiceCard("Zomato",  "🍕", "ORDER_FOOD",
                eta1 + " mins", "₹" + (49 + rng.nextInt(30)) + " delivery", "4." + rng.nextInt(9)),
            new ServiceCard("Swiggy",  "🛵", "ORDER_FOOD",
                eta2 + " mins", "₹" + (39 + rng.nextInt(30)) + " delivery", "4." + rng.nextInt(9)),
            new ServiceCard("Blinkit", "⚡", "ORDER_GROCERY",
                eta3 + " mins", "Free delivery", "4." + rng.nextInt(9)),
            new ServiceCard("Zepto",   "🟣", "ORDER_GROCERY",
                eta4 + " mins", "Free delivery", "4." + rng.nextInt(9))
        );
    }

    // ----------------------------------------------------------------
    // TRANSPORT
    // ----------------------------------------------------------------
    List<ServiceCard> transportCards(String msg) {
        int eta1 = 3 + rng.nextInt(7);
        int eta2 = 4 + rng.nextInt(8);
        int eta3 = 2 + rng.nextInt(5);
        int eta4 = 5 + rng.nextInt(10);

        return List.of(
            new ServiceCard("Uber",   "⚫", "BOOK_RIDE",
                eta1 + " mins away", "₹" + (80 + rng.nextInt(120)), "4." + rng.nextInt(9)),
            new ServiceCard("Ola",    "🟡", "BOOK_RIDE",
                eta2 + " mins away", "₹" + (75 + rng.nextInt(100)), "4." + rng.nextInt(9)),
            new ServiceCard("Rapido", "🟠", "BOOK_BIKE",
                eta3 + " mins away", "₹" + (30 + rng.nextInt(60)),  "4." + rng.nextInt(9)),
            new ServiceCard("Yulu",   "🟢", "BOOK_CYCLE",
                eta4 + " mins away", "₹" + (10 + rng.nextInt(20)),  "4." + rng.nextInt(9))
        );
    }

    // ----------------------------------------------------------------
    // SHOPPING
    // ----------------------------------------------------------------
    private List<ServiceCard> shoppingCards(String msg) {
        int p1 = 500 + rng.nextInt(2000);
        int p2 = p1 - rng.nextInt(200);
        int p3 = p1 - rng.nextInt(400);

        return List.of(
            new ServiceCard("Amazon",   "📦", "BUY_NOW",
                "Delivery tomorrow", "₹" + p1, "4." + rng.nextInt(9)),
            new ServiceCard("Flipkart", "🛍", "BUY_NOW",
                "2-day delivery",    "₹" + p2, "4." + rng.nextInt(9)),
            new ServiceCard("Meesho",   "🌸", "BUY_NOW",
                "3-5 day delivery",  "₹" + p3, "4." + rng.nextInt(9)),
            new ServiceCard("Myntra",   "👗", "BUY_NOW",
                "2-3 day delivery",  "₹" + (p1 + rng.nextInt(300)), "4." + rng.nextInt(9))
        );
    }
}
