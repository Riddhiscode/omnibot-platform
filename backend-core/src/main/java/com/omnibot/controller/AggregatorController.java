package com.omnibot.controller;

import com.omnibot.adapter.VendorAdapter;
import com.omnibot.adapter.VendorAdapterRegistry;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.agent.MockServiceAdapter;
import com.omnibot.model.Order;
import com.omnibot.model.PaymentTransaction;
import com.omnibot.repository.OrderRepository;
import com.omnibot.repository.PaymentTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregator endpoints: compare prices across vendors, place cross-vendor orders, track.
 *
 * All paths are under the context-path /api, so effective URLs are:
 *   POST /api/v1/aggregator/compare   — compare prices (uses MockServiceAdapter for ServiceCards)
 *   POST /api/v1/aggregator/search    — search via VendorAdapterRegistry (real adapter layer)
 *   POST /api/v1/aggregator/order     — place order via registry
 *   GET  /api/v1/aggregator/track     — track order via registry
 *   GET  /api/v1/aggregator/status    — vendor health status
 */
@Controller
@ResponseBody
@RequestMapping("/v1/aggregator")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080"})
public class AggregatorController {

    private final MockServiceAdapter mockServiceAdapter;
    private final VendorAdapterRegistry registry;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public AggregatorController(MockServiceAdapter mockServiceAdapter,
                                VendorAdapterRegistry registry,
                                OrderRepository orderRepository,
                                PaymentTransactionRepository paymentTransactionRepository) {
        this.mockServiceAdapter = mockServiceAdapter;
        this.registry = registry;
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  POST /v1/aggregator/compare
    //  Used by ChatEngine deal cards — returns MockServiceAdapter cards
    //  which match ChatDto.ServiceCard shape {name, logo, action, estimate, price, rating}
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> comparePrices(@RequestBody Map<String, Object> request) {
        String intent = request.get("intent") != null ? request.get("intent").toString() : "UNKNOWN";

        List<Map<String, Object>> options = mockServiceAdapter.getComparisonOptions(intent);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("options", options);
        response.put("cheapest_vendor", mockServiceAdapter.findCheapestVendor(options));
        response.put("fastest_vendor", mockServiceAdapter.findFastestVendor(options));
        response.put("vendor_count", options.size());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  POST /v1/aggregator/search
    //  Uses real VendorAdapterRegistry — returns VendorSearchResult objects
    //  Body: { "query": "biryani", "category": "FOOD", "location": "Bangalore",
    //          "latitude": 12.97, "longitude": 77.59, "maxResults": 10 }
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchAcrossVendors(@RequestBody Map<String, Object> body) {
        String query    = getString(body, "query", "");
        String catStr   = getString(body, "category", "FOOD");
        String location = getString(body, "location", "");
        double lat = getDouble(body, "latitude", 0.0);
        double lon = getDouble(body, "longitude", 0.0);
        int    max = getInt(body, "maxResults", 10);

        VendorCategory category;
        try {
            category = VendorCategory.valueOf(catStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            category = VendorCategory.FOOD;
        }

        VendorSearchRequest req = new VendorSearchRequest(query, location);
        req.setLatitude(lat);
        req.setLongitude(lon);
        req.setMaxResults(max);

        List<VendorSearchResult> results = registry.searchAll(category, req);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("category", category.name());
        response.put("query", query);
        response.put("result_count", results.size());
        response.put("results", results.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vendorName", r.getVendorName());
            m.put("itemName",   r.getItemName());
            m.put("price",      r.getPrice());
            m.put("currency",   r.getCurrency());
            m.put("etaMinutes", r.getEtaMinutes());
            m.put("available",  r.isAvailable());
            m.put("isCheapest", r.isCheapest());
            m.put("isFastest",  r.isFastest());
            m.put("description", r.getDescription());
            m.put("imageUrl",   r.getImageUrl());
            return m;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  POST /v1/aggregator/order
    //  Body: { "vendorName":"Zomato", "category":"FOOD",
    //          "deliveryAddress":"Koramangala", "amount":350, "userId":1 }
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> placeOrderViaRegistry(@RequestBody Map<String, Object> body) {
        String vendorName = getString(body, "vendorName", "");
        String catStr     = getString(body, "category", "FOOD");
        Long   userId     = getLong(body, "userId", 1L);

        VendorCategory category;
        try {
            category = VendorCategory.valueOf(catStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            category = VendorCategory.FOOD;
        }

        VendorOrderRequest req = new VendorOrderRequest();
        req.setVendorName(vendorName);
        req.setUserId(userId);
        req.setDeliveryAddress(getString(body, "deliveryAddress", ""));
        req.setAmount(BigDecimal.valueOf(getDouble(body, "amount", 0.0)));

        VendorOrderResult result = registry.placeOrder(vendorName, category, req);

        if (result.isSuccess()) {
            Order order = new Order();
            order.setUserId(userId);
            order.setVendorId(vendorName);
            order.setCategory(category.name());
            order.setStatus(category == VendorCategory.TRANSPORT ? "IN_PROGRESS" : "PREPARING");
            order.setTotalAmount(result.getAmountCharged() != null ? result.getAmountCharged() : req.getAmount());
            order.setCurrency("INR");
            order.setExternalOrderId(result.getExternalOrderId() != null ? result.getExternalOrderId() : UUID.randomUUID().toString());
            order.setTrackingUrl(result.getTrackingUrl() != null ? result.getTrackingUrl() : "https://omnibot.ai/track/" + order.getExternalOrderId());
            orderRepository.save(order);

            PaymentTransaction tx = new PaymentTransaction();
            tx.setOrderId(order.getId());
            tx.setUserId(userId);
            tx.setAmount(order.getTotalAmount());
            tx.setCurrency("INR");
            tx.setStatus("SUCCESS");
            tx.setPaymentMethod("CREDIT_CARD");
            tx.setTransactionRef("txn_" + UUID.randomUUID().toString());
            paymentTransactionRepository.save(tx);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",       result.isSuccess());
        response.put("vendorName",    result.getVendorName());
        response.put("orderId",       result.getExternalOrderId());
        response.put("trackingUrl",   result.getTrackingUrl());
        response.put("amountCharged", result.getAmountCharged());
        response.put("error",         result.getErrorMessage());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  GET /v1/aggregator/orders?userId=1
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(@RequestParam(defaultValue = "1") Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("orders", orders);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  GET /v1/aggregator/track?vendorName=Zomato&orderId=ZOM-001
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/track")
    public ResponseEntity<Map<String, Object>> trackOrder(
            @RequestParam String vendorName,
            @RequestParam String orderId) {

        VendorTrackingResult result = registry.trackOrder(vendorName, orderId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("vendorName",    result.getVendorName());
        response.put("orderId",       result.getExternalOrderId());
        response.put("status",        result.getStatus() != null ? result.getStatus().name() : null);
        response.put("statusMessage", result.getStatusMessage());
        response.put("estimatedDelivery", result.getEtaLabel());

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  GET /v1/aggregator/status
    //  Returns vendor health: which adapters are registered and available
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> vendorStatus() {
        Map<String, Object> status = registry.getVendorStatus();
        status.put("totalAdapters", registry.getAllAdapters().size());
        status.put("availableAdapters", registry.getAllAvailableAdapters().size());
        return ResponseEntity.ok(status);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────
    private String getString(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    private double getDouble(Map<String, Object> m, String key, double def) {
        Object v = m.get(key);
        if (v == null) return def;
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return def; }
    }

    private int getInt(Map<String, Object> m, String key, int def) {
        Object v = m.get(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }

    private long getLong(Map<String, Object> m, String key, long def) {
        Object v = m.get(key);
        if (v == null) return def;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return def; }
    }
}
