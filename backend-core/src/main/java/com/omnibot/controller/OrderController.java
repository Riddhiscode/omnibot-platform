package com.omnibot.controller;

import com.omnibot.adapter.VendorAdapterRegistry;
import com.omnibot.adapter.VendorCategory;
import com.omnibot.adapter.dto.*;
import com.omnibot.model.Order;
import com.omnibot.model.PaymentTransaction;
import com.omnibot.repository.OrderRepository;
import com.omnibot.repository.PaymentTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@ResponseBody
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final VendorAdapterRegistry vendorRegistry;

    public OrderController(OrderRepository orderRepository,
                           PaymentTransactionRepository paymentRepository,
                           VendorAdapterRegistry vendorRegistry) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.vendorRegistry = vendorRegistry;
    }

    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> request) {
        String vendor = (String) request.get("vendor");
        Double price = (Double) request.get("price");
        String serviceClass = (String) request.get("service_class");
        String category = (String) request.get("category");

        VendorCategory vendorCategory = resolveCategory(category, serviceClass);

        VendorOrderResult vendorResult = vendorRegistry.placeOrder(
                vendor, vendorCategory, buildOrderRequest(vendor, price));

        String trackingId = vendorResult.isSuccess() && vendorResult.getExternalOrderId() != null
                ? vendorResult.getExternalOrderId()
                : UUID.randomUUID().toString();

        Order order = new Order();
        order.setUserId(1L);
        order.setVendorId(normalizeVendorId(vendor));
        order.setCategory(vendorCategory.name());
        order.setStatus(vendorResult.isSuccess() ? "CONFIRMED" : "FAILED");
        order.setTotalAmount(vendorResult.isSuccess() && vendorResult.getAmountCharged() != null
                ? vendorResult.getAmountCharged()
                : BigDecimal.valueOf(price != null ? price : 0));
        order.setCurrency("INR");
        order.setExternalOrderId(trackingId);
        order.setTrackingUrl(vendorResult.getTrackingUrl() != null
                ? vendorResult.getTrackingUrl()
                : "https://omnibot.ai/track/" + trackingId);
        orderRepository.save(order);

        PaymentTransaction payment = new PaymentTransaction();
        payment.setOrderId(order.getId());
        payment.setUserId(order.getUserId());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("INR");
        payment.setStatus(vendorResult.isSuccess() ? "SUCCESS" : "FAILED");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setTransactionRef("txn_" + UUID.randomUUID().toString());
        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("status", vendorResult.isSuccess() ? "SUCCESS" : "FAILED");
        response.put("message", vendorResult.isSuccess()
                ? "Order placed successfully with " + vendor
                : vendorResult.getErrorMessage());
        response.put("order_id", order.getId());
        response.put("external_order_id", trackingId);
        response.put("vendor", vendor);
        response.put("category", vendorCategory.name());
        response.put("amount_charged", order.getTotalAmount());
        response.put("tracking_url", order.getTrackingUrl());
        if (vendorResult.getEstimatedDelivery() != null) {
            response.put("estimated_delivery", vendorResult.getEstimatedDelivery());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/track/{vendor}/{orderId}")
    public ResponseEntity<VendorTrackingResult> trackOrder(
            @PathVariable String vendor, @PathVariable String orderId) {
        VendorTrackingResult result = vendorRegistry.trackOrder(vendor, orderId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vendors")
    public ResponseEntity<Map<String, Object>> getVendorStatus() {
        return ResponseEntity.ok(vendorRegistry.getVendorStatus());
    }

    @PostMapping("/search")
    public ResponseEntity<List<VendorSearchResult>> searchVendors(
            @RequestParam String category,
            @RequestBody(required = false) VendorSearchRequest request) {
        VendorCategory vendorCategory;
        try {
            vendorCategory = VendorCategory.valueOf(category.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        if (request == null) {
            request = new VendorSearchRequest();
        }

        List<VendorSearchResult> results = vendorRegistry.searchAll(vendorCategory, request);
        return ResponseEntity.ok(results);
    }

    private VendorOrderRequest buildOrderRequest(String vendor, Double price) {
        VendorOrderRequest req = new VendorOrderRequest();
        req.setUserId(1L);
        req.setVendorName(vendor);
        req.setAmount(price != null ? BigDecimal.valueOf(price) : BigDecimal.ZERO);
        req.setCurrency("INR");
        req.setPaymentMethod("CREDIT_CARD");
        return req;
    }

    private VendorCategory resolveCategory(String category, String serviceClass) {
        if (category != null && !category.isBlank()) {
            try {
                return VendorCategory.valueOf(category.toUpperCase());
            } catch (Exception ignored) {}
        }
        if (serviceClass != null) {
            String sc = serviceClass.toUpperCase();
            if (sc.contains("RIDE") || sc.contains("BIKE") || sc.contains("CYCLE")) {
                return VendorCategory.TRANSPORT;
            }
            if (sc.contains("FOOD") || sc.contains("GROCERY")) {
                return VendorCategory.FOOD;
            }
            if (sc.contains("BUY") || sc.contains("SHOP")) {
                return VendorCategory.SHOPPING;
            }
        }
        return VendorCategory.TRANSPORT;
    }

    private static String normalizeVendorId(String vendor) {
        if (vendor == null || vendor.isBlank()) return "UNKNOWN";
        return vendor.toUpperCase().replace("-", "").replace(" ", "");
    }
}
