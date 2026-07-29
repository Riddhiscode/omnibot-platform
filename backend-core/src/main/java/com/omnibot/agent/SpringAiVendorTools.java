package com.omnibot.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import com.omnibot.agent.dto.*;

import java.util.List;

@Component
public class SpringAiVendorTools {

    private final VendorService vendorService;

    public SpringAiVendorTools(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    // =========================================================
    // READ-ONLY TOOLS — safe to auto-execute, no side effects.
    // These only query/compare mock vendor data.
    // =========================================================

    @Tool(description = "Search available ride options across transport vendors "
            + "(Uber, Ola, Rapido, Yulu, Lyft, Bolt) between a pickup and drop-off point. "
            + "Read-only — does not book anything.")
    public List<VendorQuote> searchRides(
            @ToolParam(description = "Pickup location, e.g. 'Koramangala, Bengaluru'")
            String pickup,
            @ToolParam(description = "Drop-off location, e.g. 'Kempegowda International Airport'")
            String destination,
            @ToolParam(description = "Requested time, ISO-8601 or natural language, e.g. 'now' or '2026-07-29T18:30'")
            String requestedTime
    ) {
        return vendorService.compareTransport(pickup, destination, requestedTime);
    }

    @Tool(description = "Search food delivery options for a cuisine or dish across food vendors "
            + "(Zomato, Swiggy, UberEats, DoorDash). Read-only — does not place an order.")
    public List<VendorQuote> searchFood(
            @ToolParam(description = "Cuisine or dish, e.g. 'biryani', 'sushi'")
            String query,
            @ToolParam(description = "Delivery address")
            String deliveryAddress
    ) {
        return vendorService.compareFood(query, deliveryAddress);
    }

    @Tool(description = "Search grocery items across grocery vendors "
            + "(Blinkit, Zepto, Instacart, BigBasket, D-Mart, JioMart, Walmart). "
            + "Read-only — does not place an order.")
    public List<VendorQuote> searchGroceries(
            @ToolParam(description = "Item or list of items to search for")
            String itemQuery,
            @ToolParam(description = "Delivery address")
            String deliveryAddress
    ) {
        return vendorService.compareGroceries(itemQuery, deliveryAddress);
    }

    @Tool(description = "Search a product across shopping vendors "
            + "(Amazon, Flipkart, Meesho, Myntra, eBay). Read-only — does not place an order.")
    public List<VendorQuote> searchProducts(
            @ToolParam(description = "Product name or description")
            String productQuery
    ) {
        return vendorService.compareShopping(productQuery);
    }

    @Tool(description = "Get the current status of an existing order or ride by its order ID. "
            + "Read-only.")
    public OrderStatus getOrderStatus(
            @ToolParam(description = "The order ID previously returned from a booking/order tool")
            String orderId
    ) {
        return vendorService.trackOrder(orderId);
    }

    // =========================================================
    // WRITE TOOLS — state-changing, even against mock data.
    // These MUST NOT execute directly without user confirmation.
    // =========================================================

    @Tool(description = "Book a ride with a specific vendor. REQUIRES EXPLICIT USER "
            + "CONFIRMATION before execution — do not call this until the user has "
            + "confirmed the vendor, pickup, destination, and time.")
    public BookingConfirmation bookRide(
            @ToolParam(description = "Vendor ID from a prior searchRides result, e.g. 'UBER', 'OLA'")
            String vendorId,
            String pickup,
            String destination,
            String requestedTime
    ) {
        return vendorService.bookRide(vendorId, pickup, destination, requestedTime);
    }

    @Tool(description = "Place a food order with a specific vendor. REQUIRES EXPLICIT "
            + "USER CONFIRMATION before execution.")
    public OrderConfirmation placeFoodOrder(
            @ToolParam(description = "Vendor ID from a prior searchFood result, e.g. 'ZOMATO', 'SWIGGY'")
            String vendorId,
            String items,
            String deliveryAddress,
            String deliveryTime
    ) {
        return vendorService.placeFoodOrder(vendorId, items, deliveryAddress, deliveryTime);
    }

    @Tool(description = "Place a grocery order with a specific vendor. REQUIRES EXPLICIT "
            + "USER CONFIRMATION before execution.")
    public OrderConfirmation placeGroceryOrder(
            @ToolParam(description = "Vendor ID from a prior searchGroceries result")
            String vendorId,
            String items,
            String deliveryAddress
    ) {
        return vendorService.placeGroceryOrder(vendorId, items, deliveryAddress);
    }

    @Tool(description = "Place a shopping order with a specific vendor. REQUIRES EXPLICIT "
            + "USER CONFIRMATION before execution.")
    public OrderConfirmation placeShoppingOrder(
            @ToolParam(description = "Vendor ID from a prior searchProducts result")
            String vendorId,
            String productQuery,
            String shippingAddress
    ) {
        return vendorService.placeShoppingOrder(vendorId, productQuery, shippingAddress);
    }

    @Tool(description = "Cancel an existing order or ride. REQUIRES EXPLICIT USER "
            + "CONFIRMATION before execution.")
    public CancellationResult cancelOrder(
            @ToolParam(description = "The order ID to cancel")
            String orderId
    ) {
        return vendorService.cancelOrder(orderId);
    }
}
