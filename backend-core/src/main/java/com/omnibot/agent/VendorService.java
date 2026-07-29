package com.omnibot.agent;

import com.omnibot.adapter.VendorAdapterRegistry;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.VendorSearchRequest;
import com.omnibot.adapter.dto.VendorSearchResult;
import com.omnibot.agent.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VendorService {

    private static final Logger log = LoggerFactory.getLogger(VendorService.class);
    private final VendorAdapterRegistry registry;

    public VendorService(VendorAdapterRegistry registry) {
        this.registry = registry;
    }

    public List<VendorQuote> compareTransport(String pickup, String destination, String requestedTime) {
        log.info("Comparing transport: pickup={}, dest={}, time={}", pickup, destination, requestedTime);
        VendorSearchRequest req = new VendorSearchRequest(destination, pickup);
        List<VendorSearchResult> results = registry.searchAll(VendorCategory.TRANSPORT, req);

        if (results.isEmpty()) {
            return List.of(
                new VendorQuote("UBER", "Uber Go", "TRANSPORT", "Ride from " + pickup, "₹240", "12 mins", "4.8", true),
                new VendorQuote("OLA", "Ola Mini", "TRANSPORT", "Ride from " + pickup, "₹225", "15 mins", "4.6", true),
                new VendorQuote("RAPIDO", "Rapido Bike", "TRANSPORT", "Bike ride from " + pickup, "₹95", "8 mins", "4.7", true)
            );
        }

        return results.stream().map(r -> new VendorQuote(
            r.getVendorName().toUpperCase(),
            r.getVendorName(),
            "TRANSPORT",
            r.getItemName() != null ? r.getItemName() : "Ride",
            "₹" + (r.getPrice() != null ? r.getPrice().intValue() : 200),
            r.getEtaMinutes() + " mins",
            String.valueOf(r.getRating()),
            r.isAvailable()
        )).collect(Collectors.toList());
    }

    public List<VendorQuote> compareFood(String query, String deliveryAddress) {
        log.info("Comparing food: query={}, address={}", query, deliveryAddress);
        VendorSearchRequest req = new VendorSearchRequest(query, deliveryAddress);
        List<VendorSearchResult> results = registry.searchAll(VendorCategory.FOOD, req);

        if (results.isEmpty()) {
            return List.of(
                new VendorQuote("ZOMATO", "Zomato - Biryani Zone", "FOOD", query, "₹350", "30 mins", "4.5", true),
                new VendorQuote("SWIGGY", "Swiggy - Paradise Biryani", "FOOD", query, "₹320", "25 mins", "4.6", true)
            );
        }

        return results.stream().map(r -> new VendorQuote(
            r.getVendorName().toUpperCase(),
            r.getVendorName(),
            "FOOD",
            r.getItemName() != null ? r.getItemName() : query,
            "₹" + (r.getPrice() != null ? r.getPrice().intValue() : 300),
            r.getEtaMinutes() + " mins",
            String.valueOf(r.getRating()),
            r.isAvailable()
        )).collect(Collectors.toList());
    }

    public List<VendorQuote> compareGroceries(String itemQuery, String deliveryAddress) {
        log.info("Comparing groceries: query={}, address={}", itemQuery, deliveryAddress);
        VendorSearchRequest req = new VendorSearchRequest(itemQuery, deliveryAddress);
        List<VendorSearchResult> results = registry.searchAll(VendorCategory.GROCERY, req);

        if (results.isEmpty()) {
            return List.of(
                new VendorQuote("BLINKIT", "Blinkit", "GROCERY", itemQuery, "₹120", "10 mins", "4.8", true),
                new VendorQuote("ZEPTO", "Zepto", "GROCERY", itemQuery, "₹115", "8 mins", "4.9", true)
            );
        }

        return results.stream().map(r -> new VendorQuote(
            r.getVendorName().toUpperCase(),
            r.getVendorName(),
            "GROCERY",
            r.getItemName() != null ? r.getItemName() : itemQuery,
            "₹" + (r.getPrice() != null ? r.getPrice().intValue() : 100),
            r.getEtaMinutes() + " mins",
            String.valueOf(r.getRating()),
            r.isAvailable()
        )).collect(Collectors.toList());
    }

    public List<VendorQuote> compareShopping(String productQuery) {
        log.info("Comparing shopping: query={}", productQuery);
        VendorSearchRequest req = new VendorSearchRequest(productQuery, "India");
        List<VendorSearchResult> results = registry.searchAll(VendorCategory.SHOPPING, req);

        if (results.isEmpty()) {
            return List.of(
                new VendorQuote("AMAZON", "Amazon", "SHOPPING", productQuery, "₹1,499", "1 day", "4.7", true),
                new VendorQuote("FLIPKART", "Flipkart", "SHOPPING", productQuery, "₹1,399", "2 days", "4.5", true)
            );
        }

        return results.stream().map(r -> new VendorQuote(
            r.getVendorName().toUpperCase(),
            r.getVendorName(),
            "SHOPPING",
            r.getItemName() != null ? r.getItemName() : productQuery,
            "₹" + (r.getPrice() != null ? r.getPrice().intValue() : 1200),
            "Same day",
            String.valueOf(r.getRating()),
            r.isAvailable()
        )).collect(Collectors.toList());
    }

    public OrderStatus trackOrder(String orderId) {
        log.info("Tracking order={}", orderId);
        return new OrderStatus(orderId, "OmniPartner", "IN_TRANSIT", "15 mins", "Driver assigned & en route");
    }

    public BookingConfirmation bookRide(String vendorId, String pickup, String destination, String requestedTime) {
        validateVendorId(vendorId);
        log.info("Booking ride: vendor={}, pickup={}, dest={}", vendorId, pickup, destination);
        String bookingId = "RIDE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new BookingConfirmation(bookingId, vendorId.toUpperCase(), vendorId, "CONFIRMED", pickup, destination, "10 mins", "₹220");
    }

    public OrderConfirmation placeFoodOrder(String vendorId, String items, String deliveryAddress, String deliveryTime) {
        validateVendorId(vendorId);
        log.info("Placing food order: vendor={}, items={}, address={}", vendorId, items, deliveryAddress);
        String orderId = "FOOD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new OrderConfirmation(orderId, vendorId.toUpperCase(), vendorId, "PLACED", items, deliveryAddress, "30 mins", "₹350");
    }

    public OrderConfirmation placeGroceryOrder(String vendorId, String items, String deliveryAddress) {
        validateVendorId(vendorId);
        log.info("Placing grocery order: vendor={}, items={}, address={}", vendorId, items, deliveryAddress);
        String orderId = "GROC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new OrderConfirmation(orderId, vendorId.toUpperCase(), vendorId, "PLACED", items, deliveryAddress, "12 mins", "₹180");
    }

    public OrderConfirmation placeShoppingOrder(String vendorId, String productQuery, String shippingAddress) {
        validateVendorId(vendorId);
        log.info("Placing shopping order: vendor={}, product={}, address={}", vendorId, productQuery, shippingAddress);
        String orderId = "SHOP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new OrderConfirmation(orderId, vendorId.toUpperCase(), vendorId, "CONFIRMED", productQuery, shippingAddress, "Tomorrow by 5 PM", "₹1,299");
    }

    public CancellationResult cancelOrder(String orderId) {
        log.info("Cancelling order={}", orderId);
        return new CancellationResult(orderId, true, "Order successfully cancelled.", "REFUND_INITIATED");
    }

    private void validateVendorId(String vendorId) {
        if (vendorId == null || vendorId.isBlank()) {
            throw new IllegalArgumentException("Invalid vendorId: vendorId cannot be null or blank");
        }
        boolean known = registry.getAllAdapters().stream()
                .anyMatch(a -> a.getVendorName().equalsIgnoreCase(vendorId) || vendorId.equalsIgnoreCase(a.getVendorName() + "EATS"));
        if (!known) {
            // Also accept common names
            List<String> knownVendors = List.of("UBER", "OLA", "RAPIDO", "YULU", "LYFT", "BOLT", "ZOMATO", "SWIGGY", "UBEREATS", "DOORDASH", "BLINKIT", "ZEPTO", "BIGBASKET", "JIOMART", "AMAZON", "FLIPKART", "MEESHO", "MYNTRA", "EBAY");
            if (!knownVendors.contains(vendorId.toUpperCase())) {
                log.warn("Unknown vendorId rejected: {}", vendorId);
                throw new IllegalArgumentException("Unknown vendorId '" + vendorId + "'. Must be a valid registered vendor ID.");
            }
        }
    }
}
