package com.omnibot.agent;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IntentService {

    public Map<String, Object> parseIntent(String text) {
        String lowerText = text.toLowerCase();
        
        List<Map<String, Object>> entities = extractEntities(lowerText);
        
        boolean isRide = containsAny(lowerText, "cab", "ride", "ola", "lyft", "bolt", "rapido", "yulu", "taxi", "transport")
                || (lowerText.contains("uber") && !lowerText.contains("eats"));
        boolean isGrocery = containsAny(lowerText, "grocery", "groceries", "blinkit", "zepto", "instacart",
                "walmart", "bigbasket", "jiomart", "d-mart", "dmart", "vegetables", "milk", "eggs");
        boolean isShopping = containsAny(lowerText, "buy", "shop", "amazon", "flipkart", "meesho", "myntra", "ebay",
                "product", "mobile", "laptop", "electronics", "clothes");
        boolean isFood = !isGrocery && containsAny(lowerText, "starving", "hungry", "food", "burger", "coffee",
                "pizza", "biryani", "restaurant", "zomato", "swiggy", "doordash", "ubereats", "latte", "meal");
        
        String intent = "UNKNOWN";
        List<String> missingSlots = new ArrayList<>();
        String replyTemplate = "I didn't quite catch that. Could you rephrase?";
        
        if (isRide && isFood) {
            intent = "MULTI_INTENT_RIDE_FOOD";
            replyTemplate = "I'll book your ride and schedule your food to arrive when you do. Let me pull up the options.";
        } else if (isRide) {
            intent = "TRANSPORT_BOOK";
            if (entities.stream().noneMatch(e -> "LOCATION".equals(e.get("type")))) {
                missingSlots.add("destination");
                replyTemplate = "Where would you like to go?";
            } else if (entities.stream().noneMatch(e -> "TIME".equals(e.get("type")))) {
                missingSlots.add("time");
                replyTemplate = "When do you need the ride?";
            } else {
                replyTemplate = "Great, pulling up the cheapest and fastest ride options for you.";
            }
        } else if (isFood) {
            intent = "FOOD_ORDER";
            if (entities.stream().noneMatch(e -> "FOOD_ITEM".equals(e.get("type")))) {
                missingSlots.add("cuisine_or_item");
                replyTemplate = "What are you in the mood for?";
            } else if (entities.stream().noneMatch(e -> "LOCATION".equals(e.get("type")))) {
                missingSlots.add("delivery_address");
                replyTemplate = "Where should I deliver this?";
            } else {
                replyTemplate = "Fetching the best deals from Zomato, Swiggy, UberEats, and DoorDash.";
            }
        } else if (isGrocery) {
            intent = "GROCERY_ORDER";
            replyTemplate = "Scanning Blinkit, Zepto, Instacart, Walmart, BigBasket, D-Mart, and JioMart for you.";
        } else if (isShopping) {
            intent = "SHOPPING_ORDER";
            replyTemplate = "Comparing Amazon, Flipkart, Meesho, Myntra, and eBay for the best price.";
        } else if (containsAny(lowerText, "compare", "cheaper", "best price", "versus", " vs ")) {
            intent = "COMPARE";
            replyTemplate = "I'll compare live rates across all integrated platforms for you.";
        } else if (lowerText.contains("hi") || lowerText.contains("hello") || lowerText.contains("hey")) {
            intent = "GREETING";
            replyTemplate = "Hello! How can I help you save time and money today?";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("confidence", 0.85);
        result.put("entities", entities);
        result.put("missing_slots", missingSlots);
        result.put("reply_template", replyTemplate);
        
        return result;
    }

    private List<Map<String, Object>> extractEntities(String text) {
        List<Map<String, Object>> entities = new ArrayList<>();
        
        if (text.contains("airport")) {
            entities.add(createEntity("LOCATION", "Airport", 0.9));
        }
        if (text.contains("home")) {
            entities.add(createEntity("LOCATION", "Home", 0.9));
        }
        if (text.contains("work")) {
            entities.add(createEntity("LOCATION", "Work", 0.9));
        }
        
        Pattern timePattern = Pattern.compile("(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm))");
        Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            entities.add(createEntity("TIME", timeMatcher.group(1), 0.95));
        }
        
        if (text.contains("burger") || text.contains("spicy chicken")) {
            entities.add(createEntity("FOOD_ITEM", "Spicy Chicken Burger", 0.9));
        }
        if (text.contains("latte") || text.contains("coffee")) {
            entities.add(createEntity("FOOD_ITEM", "Iced Latte", 0.9));
        }
        
        return entities;
    }
    
    private Map<String, Object> createEntity(String type, String value, double confidence) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("type", type);
        entity.put("value", value);
        entity.put("confidence", confidence);
        return entity;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
