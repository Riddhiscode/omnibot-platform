package com.omnibot.agent;

import com.omnibot.model.ChatMessage.Intent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Generates contextual bot replies based on detected intent.
 * Can be swapped for a real LLM API call in Phase 3.
 */
@Component
public class BotReplyEngine {

    private final Random rng = new Random();

    public String generateReply(Intent intent, String userMessage) {
        return switch (intent) {
            case FOOD_ORDER             -> foodReply(userMessage);
            case GROCERY_ORDER          -> groceryReply(userMessage);
            case TRANSPORT_BOOK         -> transportReply(userMessage);
            case SHOPPING_ORDER         -> shoppingReply(userMessage);
            case MULTI_INTENT_RIDE_FOOD -> multiIntentReply();
            case TRACK_ORDER            -> trackReply();
            case COMPARE                -> compareReply();
            case GREETING               -> greetingReply();
            case HELP                   -> helpReply();
            default                     -> unknownReply();
        };
    }

    private String foodReply(String msg) {
        List<String> replies = List.of(
            "🍕 Hungry? I've found the best options near you! Here's what's available right now:",
            "🛵 Great choice! Let me compare delivery times and prices from top food apps:",
            "😋 Found " + (3 + rng.nextInt(5)) + " restaurants matching your request. Here are the fastest options:",
            "🍽️ I'm checking Zomato, Swiggy, UberEats, and DoorDash for you right now!"
        );
        return replies.get(rng.nextInt(replies.size()));
    }

    private String transportReply(String msg) {
        List<String> replies = List.of(
            "🚗 Looking for a ride? Here are the nearest available options:",
            "🏍️ Found drivers nearby! Comparing Uber, Ola, Lyft, Bolt, and Rapido for you:",
            "⚡ Great! I've checked all transport apps. Here are the fastest pickups:",
            "🚕 Booking a ride? Here's what's available with estimated arrival times:"
        );
        return replies.get(rng.nextInt(replies.size()));
    }

    private String groceryReply(String msg) {
        List<String> replies = List.of(
            "🛒 Need groceries? Scanning Blinkit, Zepto, Instacart, Walmart, BigBasket, D-Mart, and JioMart:",
            "🥬 Fresh deals incoming! Comparing quick-commerce and hyperlocal grocery platforms:",
            "⚡ Let me find the fastest grocery delivery with the lowest fees near you:",
            "🧺 Comparing Blinkit, Zepto, Instacart, and more for your grocery run!"
        );
        return replies.get(rng.nextInt(replies.size()));
    }

    private String multiIntentReply() {
        return "🔗 Smart combo mode! I'll sync your ride and food order so everything arrives together.";
    }

    private String shoppingReply(String msg) {
        List<String> replies = List.of(
            "📦 I've compared prices across Amazon, Flipkart, Meesho, Myntra, and eBay for you:",
            "🛍️ Found the best deals! Here's a price comparison across top shopping apps:",
            "💰 Great! Let me find the cheapest option with the fastest delivery:",
            "🎯 Comparing prices and delivery times across all major shopping platforms:"
        );
        return replies.get(rng.nextInt(replies.size()));
    }

    private String trackReply() {
        return "📍 To track your order, please share your Order ID and I'll check the status " +
               "across Zomato, Swiggy, Amazon, Flipkart and more. " +
               "Example: 'Track order #ZOM123456'";
    }

    private String compareReply() {
        return "⚖️ I can compare prices, delivery times, and ratings across all integrated apps! " +
               "Tell me what you're looking for — food, a ride, or a product — and I'll show you the best options side by side.";
    }

    private String greetingReply() {
        List<String> replies = List.of(
            "👋 Hey! Welcome to OmniBot — your one-stop assistant for food, rides, and shopping! What can I help you with today?",
            "😊 Hello! I'm OmniBot. I can order food, groceries, rides, and products across 20+ apps — all in one place!",
            "🤖 Hi there! I'm OmniBot. Just tell me what you need — food, transport, or shopping — and I'll handle the rest!"
        );
        return replies.get(rng.nextInt(replies.size()));
    }

    private String helpReply() {
        return "🆘 Here's what I can do for you:\n\n" +
               "🍕 **Food** — Zomato, Swiggy, UberEats, DoorDash\n" +
               "🛒 **Groceries** — Blinkit, Zepto, Instacart, Walmart, BigBasket, D-Mart, JioMart\n" +
               "🚗 **Transport** — Uber, Ola, Lyft, Bolt, Rapido, Yulu\n" +
               "📦 **Shopping** — Amazon, Flipkart, Meesho, Myntra, eBay\n" +
               "📍 **Track** — Track any order across all platforms\n" +
               "⚖️ **Compare** — Compare prices and ETAs side by side\n\n" +
               "Just tell me what you need in plain English!";
    }

    private String unknownReply() {
        List<String> replies = List.of(
            "🤔 I'm not sure I understood that. Could you try rephrasing? I can help with food orders, rides, or shopping!",
            "💬 Hmm, I didn't quite get that. Try something like 'Order pizza' or 'Book a cab to airport'.",
            "🔍 I'm still learning! Could you be more specific? I handle food, transport, and shopping across all major apps."
        );
        return replies.get(rng.nextInt(replies.size()));
    }

    // ------------------------------------------------------------
    // BOOKING FLOW QUESTIONS (slot-filling for TRANSPORT_BOOK)
    // ------------------------------------------------------------

    public String askSource() {
        return "📍 Sure! Where are you right now? (your pickup location)";
    }

    public String askDestination(String source) {
        return "🎯 Got it — pickup from **" + source + "**. Where would you like to go?";
    }

    public String askTime() {
        return "⏰ When do you need the ride? (e.g. 'now', '5:30 PM', 'in 20 mins')";
    }

    public String askSaveRoute(String source, String destination) {
        return "💾 Want me to save this route — **" + source + " → " + destination +
               "** — for faster booking next time? (yes/no)";
    }

    public String askPreference() {
        return "⭐ Last thing — do you have a preferred app? (Uber, Ola, Lyft, Bolt, Rapido, Yulu — or say 'no preference')";
    }

    public String routeSavedConfirmation() {
        return "✅ Route saved! You can book this trip faster next time.";
    }

    public String bookingSummary(String source, String destination, String time, String preference) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚗 Here's your ride summary:\n\n");
        sb.append("📍 **From:** ").append(source).append("\n");
        sb.append("🎯 **To:** ").append(destination).append("\n");
        sb.append("⏰ **Time:** ").append(time).append("\n");
        if (preference != null && !preference.equalsIgnoreCase("no preference") && !preference.isBlank()) {
            sb.append("⭐ **Preferred app:** ").append(preference).append("\n");
        }
        sb.append("\nHere are your options:");
        return sb.toString();
    }

    // ------------------------------------------------------------
    // FOOD ORDER FLOW QUESTIONS (slot-filling for FOOD_ORDER)
    // ------------------------------------------------------------

    public String askCuisine() {
        return "🍽️ What are you in the mood for? (e.g. pizza, biryani, Chinese, groceries...)";
    }

    public String askItems(String cuisine) {
        return "📝 Nice choice — **" + cuisine + "**! Any specific items or dishes you want? " +
               "(or just say 'surprise me' / 'anything popular')";
    }

    public String askAddress() {
        return "📍 Where should this be delivered? (your address)";
    }

    public String askDeliveryTime() {
        return "⏰ When do you want it delivered? (e.g. 'now', '8 PM', 'in 30 mins')";
    }

    public String askSaveAddress(String address) {
        return "💾 Want me to save **" + address + "** as a delivery address for next time? (yes/no)";
    }

    public String askFoodPreference() {
        return "⭐ Last thing — do you have a preferred app? (Zomato, Swiggy, UberEats, DoorDash — or say 'no preference')";
    }

    public String addressSavedConfirmation() {
        return "✅ Address saved! Future orders will be faster.";
    }

    public String foodOrderSummary(String cuisine, String items, String address, String time, String preference) {
        StringBuilder sb = new StringBuilder();
        sb.append("🍕 Here's your order summary:\n\n");
        sb.append("🍽️ **Craving:** ").append(cuisine).append("\n");
        if (items != null && !items.isBlank()) {
            sb.append("📝 **Items:** ").append(items).append("\n");
        }
        sb.append("📍 **Deliver to:** ").append(address).append("\n");
        sb.append("⏰ **Time:** ").append(time).append("\n");
        if (preference != null && !preference.equalsIgnoreCase("no preference") && !preference.isBlank()) {
            sb.append("⭐ **Preferred app:** ").append(preference).append("\n");
        }
        sb.append("\nHere are your options:");
        return sb.toString();
    }


}
