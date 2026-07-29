package com.omnibot.agent.llm;

import java.util.List;
import java.util.Map;

/**
 * Defines all tool schemas that OmniCore can invoke.
 *
 * Security notes:
 * - Tool schemas are built server-side and never exposed to the client.
 * - Booking/order/buy tools have an explicit "requiresConfirmation" flag
 *   enforced by OmniCoreOrchestrator — the LLM cannot bypass this.
 * - Input parameters are validated before being passed to VendorAdapterRegistry.
 */
public final class OmniCoreTools {

    private OmniCoreTools() {}

    // Tools that require user confirmation before execution
    public static final List<String> CONFIRMATION_REQUIRED = List.of(
        "tool_mobility_book",
        "tool_food_order",
        "tool_commerce_buy"
    );

    public static List<Map<String, Object>> getAllToolSchemas() {
        return List.of(
            mobilityQuote(),
            mobilityBook(),
            foodSearch(),
            foodOrder(),
            commerceSearch(),
            commerceBuy(),
            authHandshake()
        );
    }

    // ----------------------------------------------------------------
    // TRANSPORT TOOLS
    // ----------------------------------------------------------------

    private static Map<String, Object> mobilityQuote() {
        return Map.of(
            "name", "tool_mobility_quote",
            "description", "Get ride price quotes and ETAs from Uber, Ola, Rapido, and Yulu for a given route. Call this before booking.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "origin",      Map.of("type", "string", "description", "Pickup location — address or landmark"),
                    "destination", Map.of("type", "string", "description", "Drop location — address or landmark"),
                    "ride_class",  Map.of("type", "string", "enum", List.of("economy", "premium", "bike", "auto"),
                                         "description", "Preferred vehicle class"),
                    "departure_time", Map.of("type", "string", "description", "When the user wants the ride — 'now' or ISO-8601 time")
                ),
                "required", List.of("origin", "destination")
            )
        );
    }

    private static Map<String, Object> mobilityBook() {
        return Map.of(
            "name", "tool_mobility_book",
            "description", "Book a confirmed ride. ONLY call this after the user has explicitly confirmed the Execution Summary Card.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "vendor",          Map.of("type", "string", "description", "Chosen vendor: UBER, OLA, RAPIDO, YULU"),
                    "origin",          Map.of("type", "string"),
                    "destination",     Map.of("type", "string"),
                    "ride_class",      Map.of("type", "string"),
                    "quoted_price",    Map.of("type", "string", "description", "Price shown to user at quote stage"),
                    "confirmation_id", Map.of("type", "string", "description", "Server-issued confirmation token — required")
                ),
                "required", List.of("vendor", "origin", "destination", "confirmation_id")
            )
        );
    }

    // ----------------------------------------------------------------
    // FOOD TOOLS
    // ----------------------------------------------------------------

    private static Map<String, Object> foodSearch() {
        return Map.of(
            "name", "tool_food_search",
            "description", "Search for food options across Zomato, Swiggy, Blinkit, and Zepto. Returns availability, ETAs, and delivery fees.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "cuisine",          Map.of("type", "string", "description", "Cuisine type or dish name e.g. pizza, biryani, groceries"),
                    "delivery_address", Map.of("type", "string", "description", "Full delivery address"),
                    "max_delivery_fee", Map.of("type", "number", "description", "Maximum acceptable delivery fee in INR")
                ),
                "required", List.of("cuisine", "delivery_address")
            )
        );
    }

    private static Map<String, Object> foodOrder() {
        return Map.of(
            "name", "tool_food_order",
            "description", "Place a food order. ONLY call this after the user has explicitly confirmed the Execution Summary Card.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "vendor",          Map.of("type", "string", "description", "Chosen vendor: ZOMATO, SWIGGY, BLINKIT, ZEPTO"),
                    "cuisine",          Map.of("type", "string"),
                    "items",            Map.of("type", "string", "description", "Comma-separated list of items ordered"),
                    "delivery_address", Map.of("type", "string"),
                    "quoted_eta",      Map.of("type", "string"),
                    "quoted_fee",      Map.of("type", "string"),
                    "confirmation_id", Map.of("type", "string", "description", "Server-issued confirmation token — required")
                ),
                "required", List.of("vendor", "cuisine", "delivery_address", "confirmation_id")
            )
        );
    }

    // ----------------------------------------------------------------
    // COMMERCE TOOLS
    // ----------------------------------------------------------------

    private static Map<String, Object> commerceSearch() {
        return Map.of(
            "name", "tool_commerce_search",
            "description", "Search for products across Amazon, Flipkart, Meesho, Myntra, and eBay. Returns prices, ratings, and delivery times.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "query",     Map.of("type", "string", "description", "Product name or description to search for"),
                    "category",  Map.of("type", "string", "description", "Optional product category e.g. electronics, clothing, books"),
                    "max_price", Map.of("type", "number", "description", "Maximum price in INR")
                ),
                "required", List.of("query")
            )
        );
    }

    private static Map<String, Object> commerceBuy() {
        return Map.of(
            "name", "tool_commerce_buy",
            "description", "Purchase a product. ONLY call this after the user has explicitly confirmed the Execution Summary Card.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "vendor",          Map.of("type", "string", "description", "Chosen vendor: AMAZON, FLIPKART, MEESHO, MYNTRA"),
                    "product_name",    Map.of("type", "string"),
                    "quoted_price",    Map.of("type", "string"),
                    "delivery_address", Map.of("type", "string"),
                    "confirmation_id", Map.of("type", "string", "description", "Server-issued confirmation token — required")
                ),
                "required", List.of("vendor", "product_name", "confirmation_id")
            )
        );
    }

    // ----------------------------------------------------------------
    // AUTH TOOL
    // ----------------------------------------------------------------

    private static Map<String, Object> authHandshake() {
        return Map.of(
            "name", "tool_auth_handshake",
            "description", "Initiate OAuth flow for a vendor that requires user authentication. Returns a secure redirect URI.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "vendor",   Map.of("type", "string", "description", "Vendor requiring auth: UBER, ZOMATO, AMAZON etc."),
                    "scopes",   Map.of("type", "array", "items", Map.of("type", "string"),
                                      "description", "OAuth scopes required e.g. ['read_orders', 'place_order']")
                ),
                "required", List.of("vendor")
            )
        );
    }
}
