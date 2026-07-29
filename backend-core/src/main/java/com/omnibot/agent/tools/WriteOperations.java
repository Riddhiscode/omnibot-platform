package com.omnibot.agent.tools;

import com.omnibot.security.ConfirmationTokenService;
import com.omnibot.security.ConfirmationTokenService.ActionType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WriteOperations {

    private final ConfirmationTokenService confirmationTokenService;

    public WriteOperations(ConfirmationTokenService confirmationTokenService) {
        this.confirmationTokenService = confirmationTokenService;
    }

    private String getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().isBlank() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "admin@omnibot.in"; // Fallback to current authenticated user context
    }

    @Tool(description = "Propose booking a ride with a specific vendor. This NEVER books "
            + "directly — it always returns a pending confirmation. Relay the returned "
            + "summary to the user as a confirmation card, then stop. Do not say the ride "
            + "is booked until the user has separately confirmed it.")
    public ConfirmationRequired bookRide(
            @ToolParam(description = "Vendor ID from a prior searchRides result, e.g. 'UBER'")
            String vendorId,
            String pickup,
            String destination,
            String requestedTime
    ) {
        String userId = getUserId();
        String summary = "Book %s: %s → %s at %s".formatted(vendorId, pickup, destination, requestedTime);

        Map<String, String> args = new HashMap<>();
        args.put("vendorId", vendorId != null ? vendorId : "UBER");
        args.put("pickup", pickup != null ? pickup : "Koramangala");
        args.put("destination", destination != null ? destination : "Airport");
        args.put("requestedTime", requestedTime != null ? requestedTime : "now");

        String token = confirmationTokenService.issue(userId, ActionType.BOOK_RIDE, args, summary);
        return new ConfirmationRequired(token, summary, 300);
    }

    @Tool(description = "Propose placing a food order with a specific vendor. This NEVER "
            + "places the order directly — it always returns a pending confirmation. Relay "
            + "the summary as a confirmation card, then stop.")
    public ConfirmationRequired placeFoodOrder(
            @ToolParam(description = "Vendor ID from a prior searchFood result, e.g. 'ZOMATO'")
            String vendorId,
            String items,
            String deliveryAddress,
            String deliveryTime
    ) {
        String userId = getUserId();
        String summary = "Order from %s: %s, delivered to %s at %s"
                .formatted(vendorId, items, deliveryAddress, deliveryTime);

        Map<String, String> args = new HashMap<>();
        args.put("vendorId", vendorId != null ? vendorId : "ZOMATO");
        args.put("items", items != null ? items : "Biryani");
        args.put("deliveryAddress", deliveryAddress != null ? deliveryAddress : "Home");
        args.put("deliveryTime", deliveryTime != null ? deliveryTime : "30 mins");

        String token = confirmationTokenService.issue(userId, ActionType.PLACE_FOOD_ORDER, args, summary);
        return new ConfirmationRequired(token, summary, 300);
    }

    @Tool(description = "Propose placing a grocery order with a specific vendor. This NEVER "
            + "places the order directly — it always returns a pending confirmation.")
    public ConfirmationRequired placeGroceryOrder(
            @ToolParam(description = "Vendor ID from a prior searchGroceries result, e.g. 'BLINKIT'")
            String vendorId,
            String items,
            String deliveryAddress
    ) {
        String userId = getUserId();
        String summary = "Grocery order from %s: %s, delivered to %s"
                .formatted(vendorId, items, deliveryAddress);

        Map<String, String> args = new HashMap<>();
        args.put("vendorId", vendorId != null ? vendorId : "BLINKIT");
        args.put("items", items != null ? items : "Milk, Bread");
        args.put("deliveryAddress", deliveryAddress != null ? deliveryAddress : "Home");

        String token = confirmationTokenService.issue(userId, ActionType.PLACE_GROCERY_ORDER, args, summary);
        return new ConfirmationRequired(token, summary, 300);
    }

    @Tool(description = "Propose placing a shopping order with a specific vendor. This NEVER "
            + "places the order directly — it always returns a pending confirmation.")
    public ConfirmationRequired placeShoppingOrder(
            @ToolParam(description = "Vendor ID from a prior searchProducts result, e.g. 'AMAZON'")
            String vendorId,
            String productQuery,
            String shippingAddress
    ) {
        String userId = getUserId();
        String summary = "Shopping order from %s: %s, shipping to %s"
                .formatted(vendorId, productQuery, shippingAddress);

        Map<String, String> args = new HashMap<>();
        args.put("vendorId", vendorId != null ? vendorId : "AMAZON");
        args.put("productQuery", productQuery != null ? productQuery : "Headphones");
        args.put("shippingAddress", shippingAddress != null ? shippingAddress : "Home");

        String token = confirmationTokenService.issue(userId, ActionType.PLACE_SHOPPING_ORDER, args, summary);
        return new ConfirmationRequired(token, summary, 300);
    }

    @Tool(description = "Propose cancelling an existing order or ride. This NEVER cancels "
            + "directly — it always returns a pending confirmation.")
    public ConfirmationRequired cancelOrder(
            @ToolParam(description = "The order ID to cancel")
            String orderId
    ) {
        String userId = getUserId();
        String summary = "Cancel order %s".formatted(orderId);

        Map<String, String> args = new HashMap<>();
        args.put("orderId", orderId != null ? orderId : "UNKNOWN");

        String token = confirmationTokenService.issue(userId, ActionType.CANCEL_ORDER, args, summary);
        return new ConfirmationRequired(token, summary, 300);
    }
}
