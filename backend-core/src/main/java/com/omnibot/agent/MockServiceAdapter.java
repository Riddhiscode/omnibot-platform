package com.omnibot.agent;

import com.omnibot.model.ChatDto.ServiceCard;
import com.omnibot.model.ChatMessage.Intent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock API adapters simulating parallel vendor fetches.
 * Replace each category method with real API calls once partnerships are established.
 *
 * Vendors simulated:
 *   Food delivery : Zomato, Swiggy, UberEats, DoorDash
 *   Groceries     : Blinkit, Zepto, Instacart, Walmart, BigBasket, D-Mart, JioMart
 *   Transport     : Uber, Ola, Lyft, Bolt, Rapido, Yulu
 *   Shopping      : Amazon, Flipkart, Meesho, Myntra, eBay
 */
@Component
public class MockServiceAdapter {

    public static final List<String> FOOD_VENDORS =
            List.of("Zomato", "Swiggy", "UberEats", "DoorDash");
    public static final List<String> GROCERY_VENDORS =
            List.of("Blinkit", "Zepto", "Instacart", "Walmart", "BigBasket", "D-Mart", "JioMart");
    public static final List<String> TRANSPORT_VENDORS =
            List.of("Uber", "Ola", "Lyft", "Bolt", "Rapido", "Yulu");
    public static final List<String> SHOPPING_VENDORS =
            List.of("Amazon", "Flipkart", "Meesho", "Myntra", "eBay");

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+)");

    private final Random rng = new Random();

    private static final Map<String, String> LOGO_URLS = Map.ofEntries(
            Map.entry("Zomato", "https://logo.clearbit.com/zomato.com"),
            Map.entry("Swiggy", "https://logo.clearbit.com/swiggy.com"),
            Map.entry("UberEats", "https://logo.clearbit.com/ubereats.com"),
            Map.entry("DoorDash", "https://logo.clearbit.com/doordash.com"),
            Map.entry("Blinkit", "https://logo.clearbit.com/blinkit.com"),
            Map.entry("Zepto", "https://logo.clearbit.com/zepto.com"),
            Map.entry("Instacart", "https://logo.clearbit.com/instacart.com"),
            Map.entry("Walmart", "https://logo.clearbit.com/walmart.com"),
            Map.entry("BigBasket", "https://logo.clearbit.com/bigbasket.com"),
            Map.entry("D-Mart", "https://logo.clearbit.com/dmart.in"),
            Map.entry("JioMart", "https://logo.clearbit.com/jiomart.com"),
            Map.entry("Uber", "https://logo.clearbit.com/uber.com"),
            Map.entry("Ola", "https://logo.clearbit.com/olacabs.com"),
            Map.entry("Lyft", "https://logo.clearbit.com/lyft.com"),
            Map.entry("Bolt", "https://logo.clearbit.com/bolt.eu"),
            Map.entry("Rapido", "https://logo.clearbit.com/rapido.bike"),
            Map.entry("Yulu", "https://logo.clearbit.com/yulu.bike"),
            Map.entry("Amazon", "https://logo.clearbit.com/amazon.in"),
            Map.entry("Flipkart", "https://logo.clearbit.com/flipkart.com"),
            Map.entry("Meesho", "https://logo.clearbit.com/meesho.com"),
            Map.entry("Myntra", "https://logo.clearbit.com/myntra.com"),
            Map.entry("eBay", "https://logo.clearbit.com/ebay.com")
    );

    public List<ServiceCard> getCards(Intent intent, String userMessage) {
        return switch (intent) {
            case FOOD_ORDER             -> foodCards(userMessage);
            case GROCERY_ORDER          -> groceryCards(userMessage);
            case TRANSPORT_BOOK         -> transportCards(userMessage);
            case SHOPPING_ORDER         -> shoppingCards(userMessage);
            case MULTI_INTENT_RIDE_FOOD -> multiIntentCards(userMessage);
            case COMPARE                -> compareCards(userMessage);
            default                     -> List.of();
        };
    }

    /** Used by {@code AggregatorController} — single source of truth for comparison payloads. */
    public List<Map<String, Object>> getComparisonOptions(String intentStr) {
        Intent intent;
        try {
            intent = Intent.valueOf(intentStr);
        } catch (Exception e) {
            intent = Intent.UNKNOWN;
        }

        List<ServiceCard> cards = getCards(intent, "");
        List<Map<String, Object>> options = new ArrayList<>();
        for (ServiceCard card : cards) {
            options.add(toComparisonOption(card));
        }
        return options;
    }

    public String findCheapestVendor(List<Map<String, Object>> options) {
        if (options == null || options.isEmpty()) return null;
        return options.stream()
                .min(Comparator.comparingDouble(o -> (Double) o.get("price")))
                .map(o -> (String) o.get("vendor"))
                .orElse(null);
    }

    public String findFastestVendor(List<Map<String, Object>> options) {
        if (options == null || options.isEmpty()) return null;
        return options.stream()
                .min(Comparator.comparingInt(o -> (Integer) o.get("eta_mins")))
                .map(o -> (String) o.get("vendor"))
                .orElse(null);
    }

    public List<ServiceCard> getTransportCards(String preferredApp) {
        return sortByPreference(transportCards(""), preferredApp);
    }

    public List<ServiceCard> getFoodCards(String preferredApp) {
        return sortByPreference(foodCards(""), preferredApp);
    }

    public List<ServiceCard> getGroceryCards(String preferredApp) {
        return sortByPreference(groceryCards(""), preferredApp);
    }

    public List<ServiceCard> getShoppingCards(String preferredApp) {
        return sortByPreference(shoppingCards(""), preferredApp);
    }

    // ----------------------------------------------------------------
    // FOOD DELIVERY
    // ----------------------------------------------------------------
    List<ServiceCard> foodCards(String msg) {
        int baseDelivery = 35 + rng.nextInt(25);
        return List.of(
            card("Zomato",   "🍕", "ORDER_FOOD", 20 + rng.nextInt(15), baseDelivery + rng.nextInt(20), "delivery"),
            card("Swiggy",   "🛵", "ORDER_FOOD", 25 + rng.nextInt(20), baseDelivery + rng.nextInt(15), "delivery"),
            card("UberEats", "🍔", "ORDER_FOOD", 28 + rng.nextInt(18), baseDelivery + rng.nextInt(25), "delivery"),
            card("DoorDash", "🚪", "ORDER_FOOD", 30 + rng.nextInt(22), baseDelivery - rng.nextInt(10), "delivery")
        );
    }

    // ----------------------------------------------------------------
    // GROCERIES
    // ----------------------------------------------------------------
    List<ServiceCard> groceryCards(String msg) {
        return List.of(
            card("Blinkit",   "⚡", "ORDER_GROCERY", 10 + rng.nextInt(8),  0,  "free delivery"),
            card("Zepto",     "🟣", "ORDER_GROCERY", 12 + rng.nextInt(10), 0,  "free delivery"),
            card("Instacart", "🛒", "ORDER_GROCERY", 45 + rng.nextInt(20), 49 + rng.nextInt(30), "delivery"),
            card("Walmart",   "🏪", "ORDER_GROCERY", 60 + rng.nextInt(30), 29 + rng.nextInt(20), "delivery"),
            card("BigBasket", "🧺", "ORDER_GROCERY", 90 + rng.nextInt(30), 40 + rng.nextInt(20), "delivery"),
            card("D-Mart",    "🏬", "ORDER_GROCERY", 120 + rng.nextInt(60), 0, "free over ₹499"),
            card("JioMart",   "📱", "ORDER_GROCERY", 1440, 0, "next-day free")
        );
    }

    // ----------------------------------------------------------------
    // TRANSPORT
    // ----------------------------------------------------------------
    List<ServiceCard> transportCards(String msg) {
        int baseFare = 70 + rng.nextInt(40);
        return List.of(
            card("Uber",   "⚫", "BOOK_RIDE",  3 + rng.nextInt(7),  baseFare + rng.nextInt(80),  "fare"),
            card("Ola",    "🟡", "BOOK_RIDE",  4 + rng.nextInt(8),  baseFare + rng.nextInt(60),  "fare"),
            card("Lyft",   "🩷", "BOOK_RIDE",  5 + rng.nextInt(9),  baseFare + rng.nextInt(70),  "fare"),
            card("Bolt",   "🟢", "BOOK_RIDE",  6 + rng.nextInt(10), baseFare - rng.nextInt(20),  "fare"),
            card("Rapido", "🟠", "BOOK_BIKE",  2 + rng.nextInt(5),  30 + rng.nextInt(50),        "fare"),
            card("Yulu",   "🔵", "BOOK_CYCLE", 5 + rng.nextInt(10), 10 + rng.nextInt(20),        "fare")
        );
    }

    // ----------------------------------------------------------------
    // SHOPPING / E-COMMERCE
    // ----------------------------------------------------------------
    List<ServiceCard> shoppingCards(String msg) {
        int basePrice = 500 + rng.nextInt(2000);
        return List.of(
            card("Amazon",   "📦", "BUY_NOW", 1440, basePrice,              "total"),
            card("Flipkart", "🛍", "BUY_NOW", 2880, basePrice - rng.nextInt(200), "total"),
            card("Meesho",   "🌸", "BUY_NOW", 4320, basePrice - rng.nextInt(400), "total"),
            card("Myntra",   "👗", "BUY_NOW", 2880, basePrice + rng.nextInt(300), "total"),
            card("eBay",     "🏷", "BUY_NOW", 5760, basePrice - rng.nextInt(350), "total")
        );
    }

    // ----------------------------------------------------------------
    // MULTI-INTENT (ride + food combo)
    // ----------------------------------------------------------------
    List<ServiceCard> multiIntentCards(String msg) {
        ServiceCard bestRide = transportCards(msg).stream()
                .min(Comparator.comparingInt(c -> parseEtaMins(c.getEstimate())))
                .orElseThrow();
        ServiceCard bestFood = foodCards(msg).stream()
                .min(Comparator.comparingInt(c -> parsePriceInr(c.getPrice())))
                .orElseThrow();

        int comboPrice = parsePriceInr(bestRide.getPrice()) + parsePriceInr(bestFood.getPrice()) - 40;
        List<ServiceCard> cards = new ArrayList<>();
        cards.add(new ServiceCard(
                "OmniBot Combo",
                "⚡",
                "BOOK_COMBO",
                "Synchronized arrival",
                "₹" + comboPrice + " combined",
                "4." + rng.nextInt(9)
        ));
        cards.add(new ServiceCard(
                bestRide.getName() + " + " + bestFood.getName(),
                "🔗",
                "BOOK_COMBO",
                bestRide.getEstimate(),
                "₹" + comboPrice,
                "4." + rng.nextInt(9)
        ));
        cards.addAll(transportCards(msg).subList(0, 2));
        cards.addAll(foodCards(msg).subList(0, 2));
        return cards;
    }

    // ----------------------------------------------------------------
    // GENERIC COMPARE — picks category from message keywords
    // ----------------------------------------------------------------
    List<ServiceCard> compareCards(String msg) {
        String lower = msg == null ? "" : msg.toLowerCase();
        if (containsAny(lower, "cab", "ride", "uber", "ola", "lyft", "bolt", "transport")) {
            return transportCards(msg);
        }
        if (containsAny(lower, "grocery", "groceries", "blinkit", "zepto", "instacart", "walmart", "milk", "vegetable")) {
            return groceryCards(msg);
        }
        if (containsAny(lower, "buy", "shop", "amazon", "flipkart", "ebay", "product", "mobile", "laptop")) {
            return shoppingCards(msg);
        }
        return foodCards(msg);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private ServiceCard card(String name, String logo, String action, int etaMins, int priceInr, String priceLabel) {
        String eta = formatEta(name, action, etaMins);
        String price = formatPrice(priceInr, priceLabel);
        return new ServiceCard(name, logo, action, eta, price, "4." + rng.nextInt(9));
    }

    private String formatEta(String name, String action, int etaMins) {
        if ("BUY_NOW".equals(action)) {
            if (etaMins >= 1440) return etaMins / 1440 + "-day delivery";
            return etaMins / 60 + "-" + (etaMins / 60 + 1) + " day delivery";
        }
        if ("BOOK_RIDE".equals(action) || "BOOK_BIKE".equals(action) || "BOOK_CYCLE".equals(action)) {
            return etaMins + " mins away";
        }
        if ("ORDER_GROCERY".equals(action) && "JioMart".equals(name)) {
            return "Next day";
        }
        return etaMins + " mins";
    }

    private String formatPrice(int priceInr, String priceLabel) {
        return switch (priceLabel) {
            case "free delivery" -> "Free delivery";
            case "free over ₹499" -> "Free over ₹499";
            case "next-day free" -> "Free next-day";
            case "delivery" -> "₹" + priceInr + " delivery";
            case "fare" -> "₹" + priceInr;
            case "total" -> "₹" + priceInr;
            default -> "₹" + priceInr;
        };
    }

    private List<ServiceCard> sortByPreference(List<ServiceCard> cards, String preferredApp) {
        if (preferredApp == null || preferredApp.isBlank()
                || preferredApp.equalsIgnoreCase("No preference")) {
            return cards;
        }
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

    private Map<String, Object> toComparisonOption(ServiceCard card) {
        Map<String, Object> option = new HashMap<>();
        option.put("vendor", card.getName());
        option.put("service_class", card.getAction());
        option.put("price", (double) parsePriceInr(card.getPrice()));
        option.put("eta", card.getEstimate());
        option.put("eta_mins", parseEtaMins(card.getEstimate()));
        option.put("logo_url", LOGO_URLS.getOrDefault(card.getName(), ""));
        option.put("rating", card.getRating());
        return option;
    }

    int parsePriceInr(String priceStr) {
        if (priceStr == null) return 0;
        if (priceStr.toLowerCase().contains("free")) return 0;
        Matcher m = PRICE_PATTERN.matcher(priceStr);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 0;
    }

    int parseEtaMins(String etaStr) {
        if (etaStr == null) return 999;
        String lower = etaStr.toLowerCase();
        if (lower.contains("synchronized") || lower.contains("next day")) return 1440;
        Matcher m = PRICE_PATTERN.matcher(lower);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 999;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
