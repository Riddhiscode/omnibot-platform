package com.omnibot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook listener for real-time order status updates from Swiggy Developer Portal.
 * Handles events: ORDER_ACCEPTED, FOOD_PREPARING, DRIVER_DISPATCHED, DELIVERED.
 */
@RestController
@RequestMapping({"/api/v1/webhooks/swiggy", "/v1/webhooks/swiggy"})
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class SwiggyWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SwiggyWebhookController.class);

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> handleStatusWebhook(
            @RequestHeader(value = "x-swiggy-signature", required = false) String signature,
            @RequestBody Map<String, Object> payload) {

        log.info("Received Swiggy status webhook signature={}: {}", signature, payload);

        String orderId = (String) payload.getOrDefault("order_id", "UNKNOWN");
        String eventType = (String) payload.getOrDefault("event_type", "STATUS_UPDATE");
        String status = (String) payload.getOrDefault("status", "PROCESSING");

        log.info("Swiggy Webhook Event: orderId={}, eventType={}, status={}", orderId, eventType, status);

        // Webhook processing logic - updates tracking state
        return ResponseEntity.ok(Map.of(
            "success", true,
            "order_id", orderId,
            "status", "ACKNOWLEDGED"
        ));
    }
}
